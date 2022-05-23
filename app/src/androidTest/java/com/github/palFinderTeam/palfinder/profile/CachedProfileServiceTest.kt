package com.github.palFinderTeam.palfinder.profile

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService.Companion.PROFILE_COLL
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.USERNAME_KEY
import com.github.palFinderTeam.palfinder.profile.ProfileUser.Companion.toProfileUser
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.UIMockContextServiceModule
import com.github.palFinderTeam.palfinder.utils.UIMockTimeServiceModule
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class CachedProfileServiceTest {
    private lateinit var firebaseProfileService: CachedProfileService
    private lateinit var db: FirebaseFirestore
    private lateinit var profile: ProfileUser
    private lateinit var profile2: ProfileUser

    @Before
    fun setUp() {
        // Connect db to local emulator
        db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setHost("10.0.2.2:8080")
            .setSslEnabled(false)
            .setPersistenceEnabled(false)
            .build()
        db.firestoreSettings = settings

        val timeService = UIMockTimeServiceModule.UIMockTimeService().setDate(Calendar.getInstance().apply { time = Date(0) })
        val context = UIMockContextServiceModule.UIMockContextService()

        DictionaryCache.clearAllTempCaches(context.get())

        firebaseProfileService = CachedProfileService(FirebaseProfileService(db), timeService, context)

        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val date2 = Calendar.getInstance().apply { time = Date(1) }

        profile = ProfileUser(
            "dummy",
            "Mike",
            "ljor",
            "dan",
            date1,
            ImageInstance("imageURL"),
            "Hi I'm Mike.",
            date2
        )
        profile2 = profile.copy(username = "Jordan", uuid = "whatever")
    }

    @Test
    fun createUserAddToDB() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, notNullValue())
        id!!.let {
            val userInDb = db.collection(PROFILE_COLL).document(it).get().await().toProfileUser()
            assertThat(userInDb, notNullValue())
            assertThat(userInDb, `is`(profile))
            // Make sure to clean for next tests
            db.collection(PROFILE_COLL).document(it).delete().await()
        }
    }

    @Test
    fun fetchNonExistingUserReturnsNull() = runTest {
        val nonExistingUser = firebaseProfileService.fetch("Nani")
        assertThat(nonExistingUser, nullValue())
    }

    @Test
    fun fetchNonExistingUserFlowReturnsError() = runTest {
        val nonExistingUserFlow = firebaseProfileService.fetchFlow("WTF")
        val nonExistingUser = nonExistingUserFlow.take(2).toList()
        assertThat(nonExistingUser[0], instanceOf(Response.Loading::class.java))
        assertThat(nonExistingUser[1], instanceOf(Response.Failure::class.java))
    }

    @Test
    fun editNonExistingUserReturnsNull() = runTest {
        val nonExistingId = firebaseProfileService.edit("HAHA", "dw", 4)
        assertThat(nonExistingId, nullValue())
    }

    @Test
    fun editNonExistingUserWithEntireNewProfileReturnsNull() = runTest {
        val nonExistingId = firebaseProfileService.edit("HAHA", profile)
        assertThat(nonExistingId, nullValue())
    }

    @Test
    fun fetchUserReturnRightInfo() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, notNullValue())
        id!!.let {
            val fetchedUser = firebaseProfileService.fetch(it)
            assertThat(fetchedUser, notNullValue())
            assertThat(fetchedUser, `is`(profile))
            // Make sure to clean for next tests
            db.collection(PROFILE_COLL).document(it).delete().await()
        }
    }

    @Test
    fun fetchUsersReturnRightInfo() = runTest {
        val id1 = firebaseProfileService.create(profile)
        val id2 = firebaseProfileService.create(profile2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        if (id1 != null && id2 != null) {
            val fetchedUser = firebaseProfileService.fetch(listOf(id1, id2))
            val cachedUser = firebaseProfileService.fetch(listOf(id1, id2))
            assertThat(fetchedUser, notNullValue())
            assertThat(fetchedUser, hasItems(profile, profile2))
            // Make sure to clean for next tests
            db.collection(PROFILE_COLL).document(id1).delete().await()
            db.collection(PROFILE_COLL).document(id2).delete().await()
        }
    }

    @Test
    fun muteMeetupWork() = runTest {
        val id = firebaseProfileService.create(profile)!!
        val meetup = "dummy"
        assert(!db.collection(PROFILE_COLL).document(id!!).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
        firebaseProfileService.unMuteMeetup(firebaseProfileService.fetch(id)!!, meetup)
        assert(!db.collection(PROFILE_COLL).document(id).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
        firebaseProfileService.muteMeetup(firebaseProfileService.fetch(id)!!, meetup)
        assert(db.collection(PROFILE_COLL).document(id).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
        firebaseProfileService.unMuteMeetup(firebaseProfileService.fetch(id)!!, meetup)
        assert(!db.collection(PROFILE_COLL).document(id).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
    }

    @Test
    fun unMuteMeetupWork() = runTest {
        val id = firebaseProfileService.create(profile)!!
        val meetup = "dummy"
        firebaseProfileService.muteMeetup(firebaseProfileService.fetch(id)!!, meetup!!)
        assert(db.collection(PROFILE_COLL).document(id!!).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
        firebaseProfileService.muteMeetup(firebaseProfileService.fetch(id)!!, meetup)
        assert(db.collection(PROFILE_COLL).document(id).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
        firebaseProfileService.unMuteMeetup(firebaseProfileService.fetch(id)!!, meetup)
        assert(!db.collection(PROFILE_COLL).document(id).get().await().toProfileUser()!!.mutedMeetups.contains(meetup))
    }

    @Test
    fun fetchUserFlowReturnRightInfoAndBehaveAsExpected() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, notNullValue())
        id!!.let {
            val fetchedUserFlow = firebaseProfileService.fetchFlow(it)
            val cachedFlow = firebaseProfileService.fetchFlow(it)
            val loading = fetchedUserFlow.take(2).toList()
            assertThat(loading[0], instanceOf(Response.Loading::class.java))
            assertThat(loading[1], `is`(Response.Success(profile)))

            // Make sure to clean for next tests
            db.collection(PROFILE_COLL).document(it).delete().await()
        }
    }


    @Test
    fun editingMeetupEditInDB() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, notNullValue())
        id!!.let {
            val newUsername = "Romain"
            val sameId =
                firebaseProfileService.edit(it, profile.copy(username = newUsername))
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val profileInDb = db.collection(PROFILE_COLL).document(it).get().await().toProfileUser()
            assertThat(profileInDb, `is`(profile.copy(username = newUsername)))
            // Make sure to clean for next tests
            db.collection(PROFILE_COLL).document(it).delete().await()
        }
    }

    @Test
    fun editingOneFieldEditInDb() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, notNullValue())
        id!!.let {
            val newUsername = "Romain"
            val sameId = firebaseProfileService.edit(it, USERNAME_KEY, newUsername)
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val profileInDb = db.collection(PROFILE_COLL).document(it).get().await().toProfileUser()
            assertThat(profileInDb, `is`(profile.copy(uuid = it, username = newUsername)))
            // Make sure to clean for next tests
            db.collection(FirebaseMeetUpService.MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun addNewProfileUseSameIdAsProfile() = runTest {
        val id = firebaseProfileService.create(profile)
        assertThat(id, `is`(profile.uuid))
    }

    @Test
    fun getLoggedInUser() = runTest {
        val userId = firebaseProfileService.getLoggedInUserID()
    }

    @Test
    fun getAllContainsMeetup() = runTest {
        val userId = firebaseProfileService.create(profile)
        val flow = firebaseProfileService.fetchAll(Calendar.getInstance().apply { time = Date(0) })
        val lst = flow.take(1).toList()[0]
        assertThat(lst, hasItems(profile))
    }

    @Test
    fun exitsTest() = runTest {
        val userId = firebaseProfileService.create(profile)
        assertThat(firebaseProfileService.exists(userId!!), `is`(true))
        assertThat(firebaseProfileService.exists("dummyasdfghj"), `is`(false))
    }
}