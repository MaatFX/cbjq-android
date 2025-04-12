package fr.mathieu.cbjq

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class ProductsUiState(val items: List<Product> = emptyList())

class ProductViewModel(application: Application) : ViewModel() {

    private val _products = MutableStateFlow(ProductsUiState())
    val products: StateFlow<ProductsUiState> = _products
    private val database by lazy { AppDatabase.getDatabase(application) }

    private val productDao = database.productDao()

    init {
        viewModelScope.launch {
           productDao.getAll().collect {
               products ->
               run {
                   val sortedProducts = products.sortedBy { it.limitDate }
                   _products.value = ProductsUiState(items = sortedProducts)
               }
           }
        }
    }

    fun addItem(product: Product) {
        viewModelScope.launch {
            productDao.insert(product)
        }

    }

    fun deleteItem(index: Int) {
        viewModelScope.launch {
            productDao.delete(_products.value.items[index])
        }
    }

    fun updateItem(product: Product) {
        Log.d("hello", product.toString())
        viewModelScope.launch {
            productDao.update(product)
        }
    }
}