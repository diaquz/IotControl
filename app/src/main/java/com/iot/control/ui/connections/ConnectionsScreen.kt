package com.iot.control.ui.connections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iot.control.R
import com.iot.control.model.Connection
import com.iot.control.model.enums.ConnectionType
import com.iot.control.ui.theme.IotControlTheme
import com.iot.control.viewmodel.ConnectionsViewModel
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    connectionsViewModel: ConnectionsViewModel,
    toDevices: (UUID) -> Unit,
) {
    val openDialog = remember { mutableStateOf(false ) }
    val dialogState by connectionsViewModel.dialogState.collectAsStateWithLifecycle()
    Scaffold(
        floatingActionButton = { NewConnectionFab(connectionsViewModel::newConnection, openDialog) },
        floatingActionButtonPosition = FabPosition.End
    ) {
        val state by connectionsViewModel.uiState.collectAsStateWithLifecycle()
        ConnectionsList(
            connections = state.connections,
            toDevices,
            {
                connectionsViewModel.loadSelectedConnection(it)
                openDialog.value = true
            },
            connectionsViewModel::deleteConnection
        )

        if(openDialog.value)
            ModalBottomSheet(
                onDismissRequest = { openDialog.value = false}
            ) {
                ConnectionDialog(
                    dialogState,
                    connectionsViewModel::updateDialogModel,
                    connectionsViewModel::saveConnection,
                    openDialog,
                    Modifier.padding(horizontal = 25.dp, vertical = 15.dp)
                )
            }
    }
}

@Composable
fun NewConnectionFab(
    newConnection: () -> Unit,
    openDialog: MutableState<Boolean>
)
{
    FloatingActionButton(
        onClick = {
            newConnection()
            openDialog.value = true
        },
        modifier = Modifier.offset(x = (-10).dp, y = (-80).dp)
    ) {
        Icon(Icons.Filled.Add, null)
    }
}

@Composable
fun ConnectionsList(
    connections: List<Connection>,
    toDeviceList: (UUID) -> Unit,
    openEditDialog: (Connection) -> Unit,
    delete: (Connection) -> Unit
) {
    LazyColumn(state=rememberLazyListState()) {
        items(connections) { connection ->
            ConnectionItem(connection, toDeviceList, openEditDialog, delete)
        }
        item {
           Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
fun ConnectionItem(
    connection: Connection,
    toDeviceList: (UUID) -> Unit,
    openEditDialog: (Connection) -> Unit,
    delete: (Connection) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .padding(all = 15.dp)
            .fillMaxWidth()
            .clickable { menuOpen = menuOpen.not() },
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Text(connection.name, style = MaterialTheme.typography.headlineSmall)
        Text(connection.address, style=MaterialTheme.typography.bodyMedium, color=MaterialTheme.colorScheme.secondary)

        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            SimpleChip(connection.type.labelId)
            SimpleChip(
                if(connection.isCredentials) R.string.credentials_label else R.string.anon_label
            )
            if(connection.type == ConnectionType.LOCAL_MQTT) SimpleChip(R.string.local_label)
            if(connection.isSsl) SimpleChip(R.string.ssl_label)
        }

        if(menuOpen) {
            ContextMenuItem(R.string.device_list_text) { toDeviceList(connection.id) }
            ContextMenuItem(R.string.edit_label) { openEditDialog(connection) }
            ContextMenuItem(R.string.delete_label) { delete(connection) }
        }
    }
}

@Composable
fun ContextMenuItem(resourceId: Int, callback: () -> Unit)
{
    Text(
        stringResource(resourceId),
        color=MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.clickable(onClick = callback).fillMaxWidth())
}

@Composable
fun SimpleChip(resourceId: Int)
{
    Surface(shape=MaterialTheme.shapes.small, shadowElevation=2.dp) {

        Text(stringResource(resourceId),
            modifier = Modifier.padding(all=5.dp) ,
            style = MaterialTheme.typography.titleSmall)
    }
}


@Preview
@Composable
fun PreviewConnectionList()
{
    IotControlTheme {
        ConnectionsList(listOf(
            Connection(name="Kitchen", type=ConnectionType.MQTT),
            Connection(name="Bedroom", type=ConnectionType.SMS)
        ), {}, {}, {})
    }
}