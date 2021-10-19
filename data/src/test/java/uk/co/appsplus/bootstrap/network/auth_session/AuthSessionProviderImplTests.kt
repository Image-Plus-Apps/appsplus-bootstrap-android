package uk.co.appsplus.bootstrap.network.auth_session

import com.squareup.moshi.Moshi
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import uk.co.appsplus.bootstrap.mocks.data.MockKeyValueStorage
import uk.co.appsplus.bootstrap.mocks.models.MockAuthToken
import uk.co.appsplus.bootstrap.mocks.models.authTokenGenerator
import uk.co.appsplus.bootstrap.network.models.AuthToken

class AuthSessionProviderImplTests : StringSpec() {
    private var _storage: MockKeyValueStorage? = null
    private val storage get() = _storage!!

    private var _authSessionProvider: AuthSessionProviderImpl? = null
    private val authSessionProvider get() = _authSessionProvider!!

    override fun beforeAny(testCase: TestCase) {
        super.beforeAny(testCase)
        _storage = MockKeyValueStorage()
        _authSessionProvider = AuthSessionProviderImpl(storage)
    }

    override fun afterAny(testCase: TestCase, result: TestResult) {
        super.afterAny(testCase, result)
        _authSessionProvider = null
        _storage = null
    }

    private fun MockAuthToken.toJson(): String {
        return Moshi
            .Builder()
            .build()
            .adapter(MockAuthToken::class.java)
            .toJson(this)
    }

    init {
        "current returns auth token from storage" {
            checkAll(Arb.authTokenGenerator()) {
                storage.items["auth_token"] = it.toJson()
                val authToken = authSessionProvider.currentToken()
                authToken?.accessToken shouldBe it.accessToken
                authToken?.refreshToken shouldBe it.refreshToken
            }
        }
        "current returns auth token from storage as MockAuthToken" {
            checkAll(Arb.authTokenGenerator()) {
                storage.items["auth_token"] = it.toJson()
                val authToken = authSessionProvider.currentToken(MockAuthToken::class.java)
                authToken?.accessToken shouldBe it.accessToken
                authToken?.refreshToken shouldBe it.refreshToken
                authToken?.extraInt shouldBe it.extraInt
                authToken?.extraString shouldBe it.extraString
            }
        }
        "current returns null if no auth token" {
            authSessionProvider.currentToken() shouldBe null
        }
        "current returns null if no auth token as MockAuthToken" {
            authSessionProvider.currentToken(MockAuthToken::class.java) shouldBe null
        }
        "replace updates auth token in storage" {
            checkAll(Arb.authTokenGenerator()) {
                authSessionProvider.replace(it)
                storage.itemAdded?.first shouldBe "auth_token"
                storage.itemAdded?.second shouldBe it.toJson()
            }
        }
        "replace null auth token removes from storage" {
            authSessionProvider.replace(null)
            storage.removedItem shouldBe "auth_token"
        }
        "deviceName return device name from storage" {
            checkAll(Arb.string()) {
                storage.items["device_name"] = it
                authSessionProvider.deviceName() shouldBe it
            }
        }
        "deviceName is set to random id if does not exist in storage" {
            authSessionProvider.deviceName()
            storage.itemAdded?.first shouldBe "device_name"
            storage.itemAdded?.second shouldNotBe null
        }
        "auth session provider calls auth session changed when changed" {
            checkAll(Arb.authTokenGenerator().orNull()) {
                runBlockingTest {
                    val authSessionList = mutableListOf<AuthToken?>()
                    val job = launch {
                        authSessionProvider
                            .authSessionChanged()
                            .toList(authSessionList)
                    }
                    authSessionProvider.replace(it)
                    authSessionList shouldBe listOf(it)
                    job.cancel()
                }
            }
        }
    }
}
