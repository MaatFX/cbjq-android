package fr.mathieu.cbjq

import android.app.Application

class ProductsApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

}