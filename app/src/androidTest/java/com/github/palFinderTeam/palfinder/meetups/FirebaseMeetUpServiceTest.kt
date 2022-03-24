package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService.Companion.MEETUP_COLL
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.Location
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class FirebaseMeetUpServiceTest {
    private lateinit var firebaseMeetUpService: FirebaseMeetUpService
    private lateinit var db: FirebaseFirestore
    private lateinit var meetUp: MeetUp

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

        firebaseMeetUpService = FirebaseMeetUpService(db)


        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val date2 = Calendar.getInstance().apply { time = Date(1) }


        meetUp = MeetUp(
            "dummy",
            "userId",
            "",
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0, 0.0),
            setOf(Category.DRINKING),
            true,
            2,
            listOf("userId", "userId2")
        )
    }

    @Test
    fun inserting_new_meetup_insert_in_DB() = runTest {
        val id = firebaseMeetUpService.createMeetUp(meetUp)
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
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val newOwner = "michelId"
            val sameId = firebaseMeetUpService.editMeetUp(it, meetUp.copy(creatorId = newOwner))
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
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val newOwner = "michelId"
            val sameId = firebaseMeetUpService.editMeetUp(it, "creator", newOwner)
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
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val fetchedMeetup = firebaseMeetUpService.getMeetUpData(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun getAllMeetUpReturnsAllMeetUps() = runTest {
        val meetUp2 = meetUp.copy(creatorId = "michel")
        val meetUp3 = meetUp.copy(creatorId = "jp")
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        val id3 = firebaseMeetUpService.createMeetUp(meetUp3)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        assertThat(id3, notNullValue())
        if (id != null && id2 != null && id3 != null) {
            val fetchedMeetups = firebaseMeetUpService.getAllMeetUps().first()
            assertThat(
                fetchedMeetups, hasItems(
                    meetUp.copy(uuid = id),
                    meetUp2.copy(uuid = id2),
                    meetUp3.copy(uuid = id3)
                )
            )
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(id).delete().await()
            db.collection(MEETUP_COLL).document(id2).delete().await()
            db.collection(MEETUP_COLL).document(id3).delete().await()
        }
    }

    @Test
    fun getMeetupAroundLocationWorksAsExpected() {
        // TODO later I don't want to do maths now
    }

    @Test
    fun getNonExistentMeetUpReturnsNull() = runTest {
        val fetchedMeetup = firebaseMeetUpService.getMeetUpData("DuGrandNimporteQuoi")
        assertThat(fetchedMeetup, nullValue())
    }

//    @Test
//    fun createMeetUpWhileDbIsNotAccessibleReturnsNull() = runTest {
//        //db.disableNetwork().await()
//        val id = firebaseMeetUpService.createMeetUp(meetUp)
//        assertThat(id, nullValue())
//    }

    @Test
    fun editNonExistingMeetUpReturnsNull() = runTest {
        val id = firebaseMeetUpService.editMeetUp("EncoreNimporteQuoi", meetUp)
        assertThat(id, nullValue())
    }

    @Test
    fun editNonExistingFieldReturnsNull() = runTest {
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val idNull = firebaseMeetUpService.editMeetUp(it, "NotAField", "NotAValue")
            assertThat(idNull, nullValue())
        }
    }
}