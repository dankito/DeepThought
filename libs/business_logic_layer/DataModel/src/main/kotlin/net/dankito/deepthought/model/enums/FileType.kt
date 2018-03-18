package net.dankito.deepthought.model.enums


enum class FileType {

    Other, // place as first one so that its numerical ordinal (important for database serialization) doesn't change when new values get added
    Document,
    Image,
    Audio,
    Video

}
