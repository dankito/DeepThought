package net.dankito.service.search.morelikethis


class SimilarItemsFinder {

//    private val testResult = ArrayList<Item>()
//
//    private fun test(defaultAnalyzer: LanguageDependentAnalyzer) {
//        itemIndexWriterAndSearcher.getIndexSearcher()?.let { indexSearcher ->
//            LuceneSearchEngine.log.info("directoryReader = ${itemIndexWriterAndSearcher.directoryReader}")
//
//            itemIndexWriterAndSearcher.directoryReader?.let {
//                val moreLikeThis = MoreLikeThis(it)
//                moreLikeThis.analyzer = defaultAnalyzer
//
//                moreLikeThis.minTermFreq = 1
//                moreLikeThis.minDocFreq = 1
//
//                val reader = StringReader("trump")
//                val query = moreLikeThis.like(reader, FieldName.ItemContent)
//
//                LuceneSearchEngine.log.info("indexSearcher = ${itemIndexWriterAndSearcher.getIndexSearcher()}")
//                val topDocs = indexSearcher.search(query, 1000)
//
//                topDocs.scoreDocs.forEach { scoreDoc ->
//                    val doc = indexSearcher.doc(scoreDoc.doc)
//                    val id = doc.getField(itemIndexWriterAndSearcher.getIdFieldName()).stringValue()
//
//                    itemIndexWriterAndSearcher.entityService.retrieve(id)?.let {
//                        testResult.add(it)
//                    }
//                }
//
//                LuceneSearchEngine.log.info("Similar items:${testResult.map { "\n$it" } }")
//            }
//        }
//    }

}