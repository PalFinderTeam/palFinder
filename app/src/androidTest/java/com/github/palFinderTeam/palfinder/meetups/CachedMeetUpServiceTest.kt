package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.FirebaseMeetUpService.Companion.MEETUP_COLL
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.profile.services.CachedProfileService
import com.github.palFinderTeam.palfinder.profile.services.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.services.FirebaseProfileService.Companion.PROFILE_COLL
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tags.Category
import com.github.palFinderTeam.palfinder.utils.*
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class CachedMeetUpServiceTest {

    private lateinit var firebaseProfileService: CachedProfileService
    private lateinit var firebaseMeetUpService: CachedMeetUpService
    private lateinit var db: FirebaseFirestore
    private lateinit var meetUp: MeetUp
    private lateinit var user1: ProfileUser
    private lateinit var user2: ProfileUser

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
        firebaseMeetUpService = CachedMeetUpService(FirebaseMeetUpService(db, firebaseProfileService), timeService, context)


        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val date2 = Calendar.getInstance().apply { time = Date(1) }

        user1 = ProfileUser(
            "userId", "Michel", "Jordan", "Surimi", Calendar.getInstance(),
            ImageInstance("")
        )
        user2 = user1.copy(uuid = "userId2")

        meetUp = MeetUp(
            "dummy",
            "userIdxc3",
            null,
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0, 0.0),
            setOf(Category.DRINKING),
            true,
            3,
            listOf("userIdxc"),
            Pair(null, null),
            CriterionGender.ALL
        )
    }

    @Test
    fun inserting_new_meetup_insert_in_DB() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun editingMeetupEditInDB() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val newOwner = "michelId"
            val sameId = firebaseMeetUpService.edit(it, meetUp.copy(creatorId = newOwner))
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it, creatorId = newOwner)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun editingOneFieldEditInDb() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val newOwner = "michelId"
            val sameId = firebaseMeetUpService.edit(it, "creator", newOwner)
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it, creatorId = newOwner)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun getMeetUpDataGetRightInfo() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val fetchedMeetup = firebaseMeetUpService.fetch(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun getMeetUpDataTwiceGetRightInfo() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            var fetchedMeetup = firebaseMeetUpService.fetch(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))

            fetchedMeetup = firebaseMeetUpService.fetch(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))

            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun getNonExistentMeetUpReturnsNull() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val fetchedMeetup = firebaseMeetUpService.fetch("DuGrandNimporteQuoi")
        assertThat(fetchedMeetup, nullValue())
    }

    @Test
    fun joinMeetUpJoinInDb() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        assertThat(userId, notNullValue())
        assertThat(userId, `is`("userId"))
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.joinMeetUp(it, userId!!, meetUp.startDate, user1)
            assertThat(result, instanceOf(Response.Success::class.java))
            assertThat(firebaseMeetUpService.getAllJoinedMeetupID(), hasItem(id))

            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId).delete().await()
        }
    }

    @Test
    fun leaveMeetUpLeaveInDb() = runTest {
        val userId2 = firebaseProfileService.create(user1)
        val userId = firebaseProfileService.create(user2)
        meetUp = meetUp.copy(creatorId = userId2!!)
        assertThat(userId, notNullValue())
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            firebaseMeetUpService.joinMeetUp(it, userId!!, meetUp.startDate, user2)
            val result = firebaseMeetUpService.leaveMeetUp(it, userId!!)
            assertThat(result, instanceOf(Response.Success::class.java))
            assertThat(firebaseMeetUpService.getAllJoinedMeetupID(), not(hasItem(id)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
        }
    }

    @Test
    fun joinNonExistingMeetUpReturnsFailure() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val result = firebaseMeetUpService.joinMeetUp("UWU", "Whatever", Calendar.getInstance(),user1)
        assertThat(result, instanceOf(Response.Failure::class.java))
    }

    @Test
    fun leaveNonExistingMeetUpReturnsFailure() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val result = firebaseMeetUpService.leaveMeetUp("UWU", "Whatever")
        assertThat(result, instanceOf(Response.Failure::class.java))
    }

    @Test
    fun leaveMeetUpWithoutJoiningBeforeReturnsFailure() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.leaveMeetUp(it, "MichelId")
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun editNonExistingMeetUpReturnsNull() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.edit("EncoreNimporteQuoi", meetUp)
        assertThat(id, nullValue())
    }

    @Test
    fun getAllContainsMeetup() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        val flow = firebaseMeetUpService.fetchAll(Calendar.getInstance().apply { time = Date(0) })
        val lst = flow.take(1).toList()[0]
        assertThat(lst.any{ it.name == meetUp.name }, `is`(true))
    }

    @Test
    fun exitsTest() = runTest {
        val userId = firebaseProfileService.create(user1)
        meetUp = meetUp.copy(creatorId = userId!!)
        val id = firebaseMeetUpService.create(meetUp)
        assertThat(firebaseMeetUpService.exists(id!!), `is`(true))
        assertThat(firebaseMeetUpService.exists("dummy"), `is`(false))
    }
}