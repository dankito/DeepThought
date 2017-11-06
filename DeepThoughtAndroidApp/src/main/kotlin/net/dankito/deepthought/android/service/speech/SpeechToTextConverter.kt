package net.dankito.deepthought.android.service.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import org.slf4j.LoggerFactory


class SpeechToTextConverter(private val context: Context) {

    companion object {
        private val log = LoggerFactory.getLogger(SpeechToTextConverter::class.java)
    }


    private var recognizer: SpeechRecognizer? = null


    private fun startSpeechToTextConversion() {
//        try {
//            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Jetzt sog wos g'scheids");
//            //... put other settings in the Intent
//            startActivityForResult(intent, 1)
//        } catch(e: Exception) { log.error("Could not start speech to text: $e") }

        try {
            if(recognizer == null) {
                recognizer = SpeechRecognizer.createSpeechRecognizer(context)
                recognizer?.setRecognitionListener(speechRecognitionListener)
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Jetzt sog wos g'scheids")
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.applicationInfo.packageName)

            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5)
            recognizer?.startListening(intent)
        } catch(e: Exception) { log.error("Could not start SpeechRecognizer: $e") }
    }

    private val speechRecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            log.info("Yeah, ready for speech recognition")
        }

        override fun onRmsChanged(rmsdB: Float) {
        }

        override fun onBufferReceived(buffer: ByteArray?) {
        }

        override fun onPartialResults(partialResults: Bundle?) {
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
        }

        override fun onBeginningOfSpeech() {
        }

        override fun onEndOfSpeech() {
        }

        override fun onError(error: Int) {
            log.info("Speech recognition error: $error")
        }

        override fun onResults(results: Bundle?) {
            val data = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            log.info("Got speech recognition results: $data")
        }

    }

}