package fr.mathieu.cbjq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import fr.mathieu.cbjq.ui.theme.CbjqTheme
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days


class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel: ProductViewModel = ProductViewModel(this.application)
        enableEdgeToEdge()
        setContent {
            CbjqTheme {
                val openNewProduct = remember { mutableStateOf(false) }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            colors = topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = Color.White,
                            ),
                            title = {
                                Text("C'est ouvert depuis quand ?")
                            }
                        )
                    },
                    content = { innerPadding ->

                        if(openNewProduct.value) {
                            ProductDialog(viewModel = viewModel,
                                onDismissRequest = { openNewProduct.value = false },
                                onAddBtnClicked = { openNewProduct.value = false })
                        }

                        ProductList(viewModel = viewModel, modifier = Modifier.padding(innerPadding))

                    },
                    floatingActionButton = {
                        FAB(onClick = { openNewProduct.value = true } )
                    },
                    floatingActionButtonPosition = FabPosition.End)

            }
        }
    }
}

@Composable
fun ProductRow(
    product: Product,
    onDelete: () -> Unit,
    onItemClicked: () -> Unit
) {
    val limitDateLocalDate: LocalDate = product.limitDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val openDateLocalDate: LocalDate? = product.openDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()

    val datePattern = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRANCE)

    val leftDaysBeforeDLC = getLeftDaysBeforeDLC(product)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(96.dp)
            .clickable(onClick = onItemClicked),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(getColorsForDLC(product))
            )

            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp)
            ) {

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )


                Text(
                    text = if (product.openDate == null) "Non ouvert"
                    else "Ouvert le ${openDateLocalDate?.format(datePattern)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = "DLC : ${limitDateLocalDate.format(datePattern)} | $leftDaysBeforeDLC jour(s) restant(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )



            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete action button",
                    tint = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun ProductList(viewModel: ProductViewModel, modifier: Modifier) {
    val products by viewModel.products.collectAsState()

    // contient les données du produit qui est cliqué ou en attente de confirmation pour suppression
    val targetProduct = remember { mutableStateOf<Product?>(null) }
    val targetProductIndex = remember { mutableStateOf<Int?>(null) }

    val openProductDialog = remember { mutableStateOf(false) }
    val deleteAlertDialogOpen = remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier) {
        itemsIndexed(products.items) { index, product ->
            ProductRow(
                product,
                onDelete = {
                    deleteAlertDialogOpen.value = true
                    targetProduct.value = product
                    targetProductIndex.value = index
            }, onItemClicked = {
                openProductDialog.value = true
                targetProduct.value = product
            })
        }
    }

    if (openProductDialog.value) {
        ProductDialog(viewModel, product = targetProduct.value, onDismissRequest = { openProductDialog.value = false }, onAddBtnClicked = { openProductDialog.value = false })
    }

    if (deleteAlertDialogOpen.value) {
        ConfirmationAlertDialog(
            onConfirmation = {
                deleteAlertDialogOpen.value = false
                viewModel.deleteItem(targetProductIndex.value!!)
            },
            onDismissRequest = {
                deleteAlertDialogOpen.value = false
            },
            dialogText = "Voulez-vous vraiment supprimer l'aliment ${targetProduct.value!!.name} ?",
            dialogTitle = "Confirmation",
            icon = Icons.Default.Info
        )
    }
}

@Composable
fun FAB(onClick: () -> Unit) {
    FloatingActionButton(onClick = onClick) {
        Icon(Icons.Filled.Add, "Floating action button.")
    }
}

@Composable
fun ProductDialog(
    viewModel: ProductViewModel,
    product: Product? = null,
    onDismissRequest: () -> Unit,
    onAddBtnClicked: () -> Unit
) {
    val name = remember { mutableStateOf(product?.name ?: "") }
    val openDate = remember { mutableStateOf(product?.openDate?.time) }
    val limitDate = remember { mutableStateOf(product?.limitDate?.time) }

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Nouvel aliment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Nom de l'aliment") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                DatePickerDocked(
                    initialSelectedDateMillis = if (product != null) product.openDate?.time else null,
                    onDateSelected = { if (it != null) openDate.value = it },
                    name = "Date d'ouverture"
                )


                DatePickerDocked(
                    initialSelectedDateMillis = product?.limitDate?.time,
                    onDateSelected = { if (it != null) limitDate.value = it },
                    name = "DLC"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Annuler", color = Color.Gray)
                    }

                    Button(
                        onClick = {

                            if (limitDate.value != null) {
                                if (product != null) {
                                    viewModel.updateItem(Product(id = product.id, name = name.value, openDate = openDate.value?.let { Date(it) }, limitDate = Date(limitDate.value!!)))
                                } else {
                                    viewModel.addItem(
                                        Product(
                                            name = name.value,
                                            openDate = openDate.value?.let { Date(it) },
                                            limitDate = Date(limitDate.value!!)
                                        )
                                    )
                                }
                                onAddBtnClicked()

                            }

                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Ajouter")
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDocked(initialSelectedDateMillis: Long?, onDateSelected: (selectedDate: Long?) -> Unit, name: String) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    val selectedDate = datePickerState.selectedDateMillis?.let {
        convertMillisToDate(it)
    } ?: ""

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { },
            label = { Text(name) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = !showDatePicker }) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select date"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        )

        if (showDatePicker) {
            Popup(
                onDismissRequest = { showDatePicker = false },
                alignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = 64.dp)
                        .shadow(elevation = 4.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = !showDatePicker},
                        confirmButton = {
                            TextButton(onClick = {
                                onDateSelected(datePickerState.selectedDateMillis)
                                showDatePicker = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDatePicker = false
                            }) {
                                Text("Cancel")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }

                }

            }
        }
    }
}

@Composable
fun ConfirmationAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = { Icon(icon, contentDescription = "Example Icon") },
        title = { Text(text = dialogTitle) },
        text = { Text(text = dialogText) },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(onClick = { onConfirmation() }) {
                Text("Confimer")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text("Annuler")
            }
        }
    )
}

fun getColorsForDLC(product: Product): Color {

    val dateNow: LocalDate = LocalDate.now()
    val productDLC: LocalDate = product.limitDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val daysLeftBeforeDLC  = ChronoUnit.DAYS.between(dateNow, productDLC)

    return when {
        daysLeftBeforeDLC <= 2 -> Color.hsv(0F, 0.85F, 0.90F) // rouge
        daysLeftBeforeDLC in 3..7 -> Color.hsv(28F, 0.85F, 0.90F) // orange
        else -> Color.hsv(145F, 0.85F, 0.90F) // vert
    }
}

fun getLeftDaysBeforeDLC(product: Product): Long {

    val dateNow: LocalDate = LocalDate.now()
    val limitDateLocalDate: LocalDate = product.limitDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    return ChronoUnit.DAYS.between(dateNow, limitDateLocalDate).days.inWholeDays
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}



