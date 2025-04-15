package fr.mathieu.cbjq

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.Date


@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "open_date") val openDate: Date?, // contient la date d'ouverture du produit
    @ColumnInfo(name = "limit_date") var limitDate: Date // peut contenir la DLC ou la date "à consommer de préférence"
)
