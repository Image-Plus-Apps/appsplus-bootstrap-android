package uk.co.appsplus.bootstrap.data.local_storage

import android.content.SharedPreferences
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import uk.co.appsplus.bootstrap.data.local_storage.key_value.KeyValueStorageImpl
import java.io.Serializable

class KeyValueStorageImplTests : StringSpec() {
    private var _sharedPrefs: SharedPreferences? = null
    private val sharedPrefs get() = _sharedPrefs!!

    private var _localStorage: KeyValueStorageImpl? = null
    private val localStorage get() = _localStorage!!

    private var _sharedPrefsEditor: SharedPreferences.Editor? = null
    private val sharedPrefsEditor get() = _sharedPrefsEditor!!

    override fun beforeEach(testCase: TestCase) {
        super.beforeEach(testCase)
        _sharedPrefs = mockk()
        _sharedPrefsEditor = mockk()
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        every { sharedPrefsEditor.putString(any(), any()) } returns sharedPrefsEditor
        every { sharedPrefsEditor.apply() } returns Unit
        every { sharedPrefsEditor.clear() } returns sharedPrefsEditor
        every { sharedPrefsEditor.remove(any()) } returns sharedPrefsEditor
        _localStorage = KeyValueStorageImpl { sharedPrefs }
    }

    override fun afterEach(testCase: TestCase, result: TestResult) {
        super.afterEach(testCase, result)
        _sharedPrefs = null
        _localStorage = null
        _sharedPrefsEditor = null
    }

    init {
        "fetch string returns string" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.string().shouldNotBeNull(),
            ) { key, value ->
                every { sharedPrefs.getString(key, any()) } returns value
                localStorage.getString(key) shouldBe value
            }
        }

        "fetch boolean returns boolean" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.bool().shouldNotBeNull(),
            ) { key, value ->
                every { sharedPrefs.getString(key, any()) } returns value.toString()
                localStorage.getBool(key) shouldBe value
            }
        }

        "fetch integer returns integer" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.int().shouldNotBeNull(),
            ) { key, value ->
                every { sharedPrefs.getString(key, any()) } returns value.toString()
                localStorage.getInt(key) shouldBe value
            }
        }

        "fetch double returns double" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.double()
                    .filter { it != Double.POSITIVE_INFINITY &&
                            it != Double.NEGATIVE_INFINITY &&
                            !it.isNaN() }
                    .shouldNotBeNull(),
            ) { key, value ->
                every { sharedPrefs.getString(key, any()) } returns value.toString()
                localStorage.getDouble(key) shouldBe value
            }
        }

        "fetch class returns class" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                subjectGenerator()
            ) { key, value ->
                every { sharedPrefs.getString(key, any()) } returns toJson(value)
                localStorage.getItem(key, MockSubject::class.java) shouldBe value
            }
        }

        "saves string" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.string(),
            ) { key, value ->
                localStorage.putItem(key, value)
                verify {
                    sharedPrefsEditor.putString(key, value)
                }
            }
        }

        "saves bool" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.bool().shouldNotBeNull(),
            ) { key, value ->
                localStorage.putItem(key, value)
                verify {
                    sharedPrefsEditor.putString(key, value.toString())
                }
            }
        }

        "saves int" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                Arb.int().shouldNotBeNull(),
            ) { key, value ->
                localStorage.putItem(key, value)
                verify {
                    sharedPrefsEditor.putString(key, value.toString())
                }
            }
        }

        "saves double" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                subjectGenerator()
            ) { key, value ->
                localStorage.putItem(key, value)
                verify {
                    sharedPrefsEditor.putString(key, toJson(value))
                }
            }
        }

        "saves serializable" {
            checkAll(
                Arb.string().filter { it.isNotBlank() },
                subjectGenerator()
            ) { key, value ->
                localStorage.putItem(key, value)
                verify {
                    sharedPrefsEditor.putString(key, toJson(value))
                }
            }
        }

        "removes key" {
            checkAll(
                Arb.string().filter { it.isNotBlank() }
            ) { key ->
                localStorage.removeItem(key)
                verify {
                    sharedPrefsEditor.remove(key)
                }
            }
        }

        "clears" {
            localStorage.clear()
            verify {
                sharedPrefsEditor.clear()
            }
        }
    }

    private fun toJson(item: MockSubject): String? {
        return Moshi.Builder().build().adapter(MockSubject::class.java).toJson(item)
    }

    @JsonClass(generateAdapter = true)
    data class MockSubject(
        val inner: Inner
    ) : Serializable {
        @JsonClass(generateAdapter = true)
        data class Inner(
            val valueString: String,
            val valueBool: Boolean,
            val valueInt: Int,
            val valueDouble: Double
        ) : Serializable
    }

    private fun subjectGenerator(
        valueString: Arb<String> = Arb.string(),
        valueBool: Arb<Boolean> = Arb.bool(),
        valueInt: Arb<Int> = Arb.int(),
        valueDouble: Arb<Double> = Arb.double()
            .filter { it != Double.POSITIVE_INFINITY &&
                    it != Double.NEGATIVE_INFINITY &&
                    !it.isNaN() }
            .shouldNotBeNull()
    ): Arb<MockSubject> {
        return Arb.bind(
            valueString,
            valueBool,
            valueInt,
            valueDouble
        ) { string,
            bool,
            int,
            double ->

            MockSubject(
                MockSubject.Inner(
                    string,
                    bool,
                    int,
                    double
                )
            )
        }
    }
}
