package net.dankito.deepthought.javafx

import javafx.application.Application
import net.dankito.deepthought.javafx.di.JavaFXExtendedInstanceProvider
import net.dankito.deepthought.javafx.di.JavaFXInstanceProvider


class DeepThoughtJavaFXExtendedApplication : DeepThoughtJavaFXApplication() {

    override fun createFlavorInstanceProvider(): JavaFXInstanceProvider {
        return JavaFXExtendedInstanceProvider()
    }

}

fun main(args: Array<String>) {
    Application.launch(DeepThoughtJavaFXExtendedApplication::class.java, *args)
}