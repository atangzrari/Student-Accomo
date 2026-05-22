package com.bac.homelink.core.utils
import android.content.Context; import androidx.datastore.core.DataStore; import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore; import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.flow.first; import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking; import javax.inject.Inject; import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("homelink_prefs")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val KEY_USER_ID    = intPreferencesKey("user_id")
        val KEY_USER_NAME  = stringPreferencesKey("user_name")
        val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        val KEY_USER_ROLE  = stringPreferencesKey("user_role")
        val KEY_LOGGED_IN  = booleanPreferencesKey("is_logged_in")
        val KEY_FILTER_PRICE = intPreferencesKey("filter_price")
        val KEY_FILTER_LOC   = stringPreferencesKey("filter_location")
        val KEY_FILTER_DATE  = stringPreferencesKey("filter_date")
        val KEY_FILTER_TYPE  = stringPreferencesKey("filter_type")
        val KEY_FILTER_SHARING = stringPreferencesKey("filter_sharing")
        val KEY_FILTER_QUERY = stringPreferencesKey("filter_query")
        val KEY_FILTER_ALERTS = booleanPreferencesKey("filter_alerts")
    }
    suspend fun saveSession(id:Int, name:String, email:String, role:String) {
        context.dataStore.edit { it[KEY_USER_ID]=id; it[KEY_USER_NAME]=name; it[KEY_USER_EMAIL]=email; it[KEY_USER_ROLE]=role; it[KEY_LOGGED_IN]=true }
    }
    suspend fun logout() { context.dataStore.edit { it.clear() } }
    fun getUserId():Int       = runBlocking { context.dataStore.data.first()[KEY_USER_ID]    ?: -1 }
    fun getUserName():String  = runBlocking { context.dataStore.data.first()[KEY_USER_NAME]  ?: "" }
    fun getUserEmail():String = runBlocking { context.dataStore.data.first()[KEY_USER_EMAIL] ?: "" }
    fun getUserRole():String  = runBlocking { context.dataStore.data.first()[KEY_USER_ROLE]  ?: "STUDENT" }
    fun isLoggedIn():Boolean  = runBlocking { context.dataStore.data.first()[KEY_LOGGED_IN]  ?: false }
    fun isLoggedInFlow():Flow<Boolean>  = context.dataStore.data.map { it[KEY_LOGGED_IN]  ?: false }
    suspend fun saveFilterPreferences(price:Int, loc:String, date:String) {
        context.dataStore.edit { it[KEY_FILTER_PRICE]=price; it[KEY_FILTER_LOC]=loc; it[KEY_FILTER_DATE]=date }
    }
    suspend fun saveFilterPreferences(
        price:Int,
        loc:String,
        date:String,
        accommodationType:String,
        sharingArrangement:String,
        query:String,
        alertsEnabled:Boolean
    ) {
        context.dataStore.edit {
            it[KEY_FILTER_PRICE] = price
            it[KEY_FILTER_LOC] = loc
            it[KEY_FILTER_DATE] = date
            it[KEY_FILTER_TYPE] = accommodationType
            it[KEY_FILTER_SHARING] = sharingArrangement
            it[KEY_FILTER_QUERY] = query
            it[KEY_FILTER_ALERTS] = alertsEnabled
        }
    }
    fun getSavedFilterPrice():Int    = runBlocking { context.dataStore.data.first()[KEY_FILTER_PRICE] ?: 0 }
    fun getSavedFilterLocation():String = runBlocking { context.dataStore.data.first()[KEY_FILTER_LOC] ?: "" }
    fun getSavedFilterDate():String  = runBlocking { context.dataStore.data.first()[KEY_FILTER_DATE] ?: "" }
    fun getSavedFilterType():String  = runBlocking { context.dataStore.data.first()[KEY_FILTER_TYPE] ?: "" }
    fun getSavedFilterSharing():String  = runBlocking { context.dataStore.data.first()[KEY_FILTER_SHARING] ?: "" }
    fun getSavedFilterQuery():String = runBlocking { context.dataStore.data.first()[KEY_FILTER_QUERY] ?: "" }
    fun getSavedFilterAlerts():Boolean = runBlocking { context.dataStore.data.first()[KEY_FILTER_ALERTS] ?: false }
}
