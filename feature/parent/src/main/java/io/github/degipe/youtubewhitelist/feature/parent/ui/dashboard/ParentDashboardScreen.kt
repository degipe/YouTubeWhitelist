package io.github.degipe.youtubewhitelist.feature.parent.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.degipe.youtubewhitelist.core.data.model.KidProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    viewModel: ParentDashboardViewModel,
    onBackToKidMode: (profileId: String) -> Unit,
    onChangePin: () -> Unit,
    onOpenWhitelistManager: (profileId: String) -> Unit,
    onOpenBrowser: (profileId: String) -> Unit,
    onOpenSleepMode: (profileId: String) -> Unit,
    onEditProfile: (profileId: String) -> Unit,
    onWatchStats: (profileId: String) -> Unit,
    onExportImport: (parentAccountId: String) -> Unit,
    onCreateProfile: () -> Unit,
    onAbout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parent Dashboard") },
                navigationIcon = {
                    IconButton(
                        onClick = { uiState.selectedProfileId?.let { onBackToKidMode(it) } },
                        enabled = uiState.selectedProfileId != null
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Kid Mode"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingContent(modifier = Modifier.padding(padding))
        } else {
            DashboardContent(
                uiState = uiState,
                onProfileSelected = viewModel::selectProfile,
                onBackToKidMode = {
                    uiState.selectedProfileId?.let { onBackToKidMode(it) }
                },
                onChangePin = onChangePin,
                onOpenWhitelistManager = {
                    uiState.selectedProfileId?.let { onOpenWhitelistManager(it) }
                },
                onOpenBrowser = {
                    uiState.selectedProfileId?.let { onOpenBrowser(it) }
                },
                onOpenSleepMode = {
                    uiState.selectedProfileId?.let { onOpenSleepMode(it) }
                },
                onEditProfile = {
                    uiState.selectedProfileId?.let { onEditProfile(it) }
                },
                onWatchStats = {
                    uiState.selectedProfileId?.let { onWatchStats(it) }
                },
                onExportImport = {
                    uiState.parentAccountId?.let { onExportImport(it) }
                },
                onCreateProfile = onCreateProfile,
                onAbout = onAbout,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun DashboardContent(
    uiState: ParentDashboardUiState,
    onProfileSelected: (String) -> Unit,
    onBackToKidMode: () -> Unit,
    onChangePin: () -> Unit,
    onOpenWhitelistManager: () -> Unit,
    onOpenBrowser: () -> Unit,
    onOpenSleepMode: () -> Unit,
    onEditProfile: () -> Unit,
    onWatchStats: () -> Unit,
    onExportImport: () -> Unit,
    onCreateProfile: () -> Unit,
    onAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile selector section
        Text(
            text = "Kid Profiles",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.profiles.isEmpty()) {
            Text(
                text = "No kid profiles yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ProfileSelector(
                profiles = uiState.profiles,
                selectedProfileId = uiState.selectedProfileId,
                onProfileSelected = onProfileSelected
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action cards
        Text(
            text = "Actions",
            style = MaterialTheme.typography.titleMedium
        )

        ActionCard(
            icon = Icons.AutoMirrored.Filled.List,
            title = "Manage Whitelist",
            subtitle = "View and edit whitelisted content for the selected profile",
            onClick = onOpenWhitelistManager,
            enabled = uiState.selectedProfileId != null
        )

        ActionCard(
            icon = Icons.Default.Search,
            title = "Browse YouTube",
            subtitle = "Browse YouTube and add content to the whitelist",
            onClick = onOpenBrowser,
            enabled = uiState.selectedProfileId != null
        )

        ActionCard(
            icon = Icons.Default.Bedtime,
            title = "Sleep Mode",
            subtitle = "Set a sleep timer with calming videos",
            onClick = onOpenSleepMode,
            enabled = uiState.selectedProfileId != null
        )

        ActionCard(
            icon = Icons.Default.Settings,
            title = "Edit Profile",
            subtitle = "Change name, avatar, and daily time limit",
            onClick = onEditProfile,
            enabled = uiState.selectedProfileId != null
        )

        ActionCard(
            icon = Icons.Default.QueryStats,
            title = "Watch Stats",
            subtitle = "View watch time statistics for the selected profile",
            onClick = onWatchStats,
            enabled = uiState.selectedProfileId != null
        )

        ActionCard(
            icon = Icons.Default.SwapHoriz,
            title = "Export / Import",
            subtitle = "Backup or restore profiles and whitelisted content",
            onClick = onExportImport,
            enabled = uiState.parentAccountId != null
        )

        ActionCard(
            icon = Icons.Default.PersonAdd,
            title = "Create Profile",
            subtitle = "Add a new kid profile",
            onClick = onCreateProfile,
            enabled = true
        )

        ActionCard(
            icon = Icons.Default.Lock,
            title = "Change PIN",
            subtitle = "Update your parent access PIN",
            onClick = onChangePin,
            enabled = true
        )

        ActionCard(
            icon = Icons.Default.Info,
            title = "About",
            subtitle = "App info, license, and support",
            onClick = onAbout,
            enabled = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBackToKidMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Kid Mode")
        }
    }
}

@Composable
private fun ProfileSelector(
    profiles: List<KidProfile>,
    selectedProfileId: String?,
    onProfileSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(profiles, key = { it.id }) { profile ->
            ProfileChip(
                profile = profile,
                isSelected = profile.id == selectedProfileId,
                onClick = { onProfileSelected(profile.id) }
            )
        }
    }
}

@Composable
private fun ProfileChip(
    profile: KidProfile,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = profile.name,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.surface
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
