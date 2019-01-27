package net.dankito.service.search.writerandsearcher.sorting

import org.apache.lucene.index.AtomicReaderContext
import org.apache.lucene.index.SortedDocValues
import org.apache.lucene.search.FieldCache
import org.apache.lucene.search.FieldComparator
import org.apache.lucene.util.BytesRef
import java.io.IOException
import java.text.Collator


/**
 * This is a copy of FieldComparator.TermOrdValComparator class which thanks to the final keyword i cannot derive from (most useless keyword ever *g*).
 * I only changed the compare() method to do sorting via a Collator so that e.g. also German Umlaute are sorted correctly.
 */
class CorrectStringFieldComparator : FieldComparator<BytesRef> {

    /* Ords for each slot.
       @lucene.internal */
    private var ords: IntArray = IntArray(0)

    /* Values for each slot.
       @lucene.internal */
    private var values: Array<BytesRef?> = arrayOf()

    /* Which reader last copied a value into the slot. When
       we compare two slots, we just compare-by-ord if the
       readerGen is the same; else we must compare the
       values (slower).
       @lucene.internal */
    private var readerGen: IntArray = IntArray(0)

    /* Gen of current reader we are on.
       @lucene.internal */
    private var currentReaderGen = -1

    /* Current reader's doc ord/values.
       @lucene.internal */
    private var termsIndex: SortedDocValues? = null

    private var field: String

    private val reversed: Boolean

    /* Bottom slot, or -1 if queue isn't full yet
       @lucene.internal */
    private var bottomSlot = -1

    /* Bottom ord (same as ords[bottomSlot] once bottomSlot
       is set).  Cached for faster compares.
       @lucene.internal */
    private var bottomOrd: Int = 0

    /* True if current bottom slot matches the current
       reader.
       @lucene.internal */
    private var bottomSameReader: Boolean = false

    /* Bottom value (same as values[bottomSlot] once
       bottomSlot is set).  Cached for faster compares.
      @lucene.internal */
    private var bottomValue: BytesRef? = null

    /** Set by setTopValue.  */
    private var topValue: BytesRef? = null
    private var topSameReader: Boolean = false
    private var topOrd: Int = 0

    private var docBase: Int = 0

    /** -1 if missing values are sorted first, 1 if they are
     * sorted last  */
    private var missingSortCmp: Int

    /** Which ordinal to use for a missing value.  */
    private var missingOrd: Int


    private val collator = Collator.getInstance()


    /** Creates this, sorting missing values first.  */
    constructor(numHits: Int, field: String, reversed: Boolean): this(numHits, field, reversed, false)

    /** Creates this, with control over how missing values
     * are sorted.  Pass sortMissingLast=true to put
     * missing values at the end.  */
    constructor(numHits: Int, field: String, reversed: Boolean, sortMissingLast: Boolean): super() {
        ords = IntArray(numHits)
        values = arrayOfNulls(numHits)
        readerGen = IntArray(numHits)

        this.field = field
        this.reversed = reversed
        this.collator.strength = Collator.SECONDARY

        if(sortMissingLast) {
            missingSortCmp = 1
            missingOrd = Integer.MAX_VALUE
        }
        else {
            missingSortCmp = -1
            missingOrd = -1
        }
    }

    override fun compare(slot1: Int, slot2: Int): Int {
        // i don't know why but for most slots readerGen stays '0'.
        // So i added 'readerGen[slot1] != 0' as otherwise doCompareValues() never gets called for them but a wrong value from ords[] gets returned
        if(readerGen[slot1] == readerGen[slot2] && readerGen[slot1] != 0) {
            return ords[slot1] - ords[slot2]
        }

        val val1 = values[slot1]
        val val2 = values[slot2]

        return doCompareValues(val1, val2)
    }

    override fun compareBottom(doc: Int): Int {
        assert(bottomSlot != -1)
        var docOrd = termsIndex?.getOrd(doc) ?: 0
        if (docOrd == -1) {
            docOrd = missingOrd
        }
        return if (bottomSameReader) {
            // ord is precisely comparable, even in the equal case
            bottomOrd - docOrd
        } else if (bottomOrd >= docOrd) {
            // the equals case always means bottom is > doc
            // (because we set bottomOrd to the lower bound in
            // setBottom):
            1
        } else {
            -1
        }
    }

    override fun copy(slot: Int, doc: Int) {
        var ord = termsIndex?.getOrd(doc) ?: 0
        if (ord == -1) {
            ord = missingOrd
            values[slot] = null
        } else {
            assert(ord >= 0)
            if (values[slot] == null) {
                values[slot] = BytesRef()
            }
            termsIndex?.lookupOrd(ord, values[slot])
        }
        ords[slot] = ord
        readerGen[slot] = currentReaderGen
    }

    @Throws(IOException::class)
    override fun setNextReader(context: AtomicReaderContext): FieldComparator<BytesRef> {
        docBase = context.docBase
        termsIndex = FieldCache.DEFAULT.getTermsIndex(context.reader(), field)
        currentReaderGen++

        if (topValue != null) {
            // Recompute topOrd/SameReader
            val ord = termsIndex?.lookupTerm(topValue) ?: 0
            if (ord >= 0) {
                topSameReader = true
                topOrd = ord
            } else {
                topSameReader = false
                topOrd = -ord - 2
            }
        } else {
            topOrd = missingOrd
            topSameReader = true
        }
        //System.out.println("  setNextReader topOrd=" + topOrd + " topSameReader=" + topSameReader);

        if (bottomSlot != -1) {
            // Recompute bottomOrd/SameReader
            setBottom(bottomSlot)
        }

        return this
    }

    override fun setBottom(bottom: Int) {
        bottomSlot = bottom

        bottomValue = values[bottomSlot]
        if (currentReaderGen == readerGen[bottomSlot]) {
            bottomOrd = ords[bottomSlot]
            bottomSameReader = true
        } else {
            if (bottomValue == null) {
                // missingOrd is null for all segments
                assert(ords[bottomSlot] == missingOrd)
                bottomOrd = missingOrd
                bottomSameReader = true
                readerGen[bottomSlot] = currentReaderGen
            } else {
                val ord = termsIndex?.lookupTerm(bottomValue) ?: 0
                if (ord < 0) {
                    bottomOrd = -ord - 2
                    bottomSameReader = false
                } else {
                    bottomOrd = ord
                    // exact value match
                    bottomSameReader = true
                    readerGen[bottomSlot] = currentReaderGen
                    ords[bottomSlot] = bottomOrd
                }
            }
        }
    }

    override fun setTopValue(value: BytesRef) {
        // null is fine: it means the last doc of the prior
        // search was missing this value
        topValue = value
        //System.out.println("setTopValue " + topValue);
    }

    override fun value(slot: Int): BytesRef? {
        return values[slot]
    }

    override fun compareTop(doc: Int): Int {

        var ord = termsIndex?.getOrd(doc) ?: 0
        if (ord == -1) {
            ord = missingOrd
        }

        return if (topSameReader) {
            // ord is precisely comparable, even in the equal
            // case
            //System.out.println("compareTop doc=" + doc + " ord=" + ord + " ret=" + (topOrd-ord));
            topOrd - ord
        } else if (ord <= topOrd) {
            // the equals case always means doc is < value
            // (because we set lastOrd to the lower bound)
            1
        } else {
            -1
        }
    }

    override fun compareValues(val1: BytesRef?, val2: BytesRef?): Int {
        return doCompareValues(val1, val2)
    }

    private fun doCompareValues(val1: BytesRef?, val2: BytesRef?): Int {
        if(val1 == null) {
            return if(val2 == null) {
                0
            } else missingSortCmp
        }
        else if (val2 == null) {
            return -missingSortCmp
        }

        val string1 = val1.utf8ToString()
        val string2 = val2.utf8ToString()

        return collator.compare(string1, string2)
    }

}