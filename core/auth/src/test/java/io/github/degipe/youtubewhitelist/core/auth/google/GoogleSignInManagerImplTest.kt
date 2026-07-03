package io.github.degipe.youtubewhitelist.core.auth.google

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GoogleSignInManagerImplTest {

    private val tokenExchanger: OAuthTokenExchanger = mockk(relaxed = true)

    @Test
    fun `signIn with blank client id fails fast without launching OAuth flow`() = runTest {
        val manager = GoogleSignInManagerImpl(tokenExchanger, clientId = "")
        val context: Context = mockk(relaxed = true)

        val result = manager.signIn(context)

        assertThat(result).isInstanceOf(GoogleSignInResult.Error::class.java)
        assertThat((result as GoogleSignInResult.Error).message)
            .isEqualTo("Google sign-in is not configured in this build")
        // No CustomTabsIntent.launchUrl call happens because the guard returns before
        // touching the activity context at all.
    }

    @Test
    fun `signIn with blank-after-trim client id also fails fast`() = runTest {
        val manager = GoogleSignInManagerImpl(tokenExchanger, clientId = "   ")
        val context: Context = mockk(relaxed = true)

        val result = manager.signIn(context)

        assertThat(result).isInstanceOf(GoogleSignInResult.Error::class.java)
    }
}
