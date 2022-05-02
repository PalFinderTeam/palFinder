package com.github.palFinderTeam.palfinder.meetups

import android.icu.util.Calendar
import android.util.Log
import com.github.palFinderTeam.palfinder.meetups.FirebaseMeetUpService.Companion.MEETUP_COLL
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.DESCRIPTION
import com.github.palFinderTeam.palfinder.meetups.MeetUp.Companion.toMeetUp
import com.github.palFinderTeam.palfinder.meetups.activities.ShowParam
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.FirebaseProfileService.Companion.PROFILE_COLL
import com.github.palFinderTeam.palfinder.profile.ProfileUser
import com.github.palFinderTeam.palfinder.tag.Category
import com.github.palFinderTeam.palfinder.utils.CriterionGender
import com.github.palFinderTeam.palfinder.utils.Gender
import com.github.palFinderTeam.palfinder.utils.Location
import com.github.palFinderTeam.palfinder.utils.Response
import com.github.palFinderTeam.palfinder.utils.image.ImageInstance
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.junit.Before
import org.junit.Test
import java.util.*

@ExperimentalCoroutinesApi
class FirebaseMeetUpServiceTest {
    private lateinit var firebaseProfileService: FirebaseProfileService
    private lateinit var firebaseMeetUpService: FirebaseMeetUpService
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

        firebaseMeetUpService = FirebaseMeetUpService(db)
        firebaseProfileService = FirebaseProfileService(db)

        val date1 = Calendar.getInstance().apply { time = Date(0) }
        val date2 = Calendar.getInstance().apply { time = Date(1) }

        user1 = ProfileUser(
            "userId", "Michel", "Jordan", "Surimi", Calendar.getInstance(),
            ImageInstance(""), following = listOf("userId2")
        )
        user2 = user1.copy(uuid = "userId2")


        meetUp = MeetUp(
            "dummy",
            "userId",
            null,
            "dummy",
            "dummy",
            date1,
            date2,
            Location(0.0, 0.0),
            setOf(Category.DRINKING),
            true,
            3,
            listOf("userId"),
            Pair(null, null),
            CriterionGender.ALL
        )
    }

    @Test
    fun inserting_new_meetup_insert_in_DB() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        assertThat(userId, notNullValue())
        id!!.let {
            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun editingMeetupEditInDB() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        assertThat(userId, notNullValue())
        id!!.let {
            val newDescription = "coucou"
            val sameId =
                firebaseMeetUpService.editMeetUp(it, meetUp.copy(description = newDescription))
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it, description = newDescription)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun editingOneFieldEditInDb() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        assertThat(userId, notNullValue())
        id!!.let {
            val newDescription = "coucou"
            val sameId = firebaseMeetUpService.editMeetUp(it, DESCRIPTION, newDescription)
            assertThat(sameId, notNullValue())
            assertThat(sameId, `is`(id))

            val meetUpInDb = db.collection(MEETUP_COLL).document(it).get().await().toMeetUp()
            assertThat(meetUpInDb, `is`(meetUp.copy(uuid = it, description = newDescription)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun getMeetUpDataGetRightInfo() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        assertThat(userId, notNullValue())
        id!!.let {
            val fetchedMeetup = firebaseMeetUpService.getMeetUpData(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun getMeetUpDataTwiceGetRightInfo() = runTest {
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            var fetchedMeetup = firebaseMeetUpService.getMeetUpData(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))

            fetchedMeetup = firebaseMeetUpService.getMeetUpData(it)
            assertThat(fetchedMeetup, notNullValue())
            assertThat(fetchedMeetup, `is`(meetUp.copy(uuid = it)))

            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
        }
    }

    @Test
    fun getAllMeetUpReturnsAllMeetUps() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(description = "michel")
        val meetUp3 = meetUp.copy(description = "jp")
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
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun getMeetupAroundLocationWorksAsExpected() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(location = Location(4.0, 4.0)) // ~ 628km
        val meetUp3 = meetUp.copy(location = Location(4.1, 4.0)) // ~ 636km
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        val id3 = firebaseMeetUpService.createMeetUp(meetUp3)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        assertThat(id3, notNullValue())
        if (id != null && id2 != null && id3 != null) {
            val fetchedMeetupsFlow =
                firebaseMeetUpService.getMeetUpsAroundLocation(meetUp.location, 630.0)
            val fetchedMeetups = fetchedMeetupsFlow.take(2).toList()
            assertThat(fetchedMeetups[0], instanceOf(Response.Loading::class.java))
            fetchedMeetups.subList(1, fetchedMeetups.size - 1).forEach {
                assertThat(it, instanceOf(Response.Success::class.java))
            }
            val meetUps =
                fetchedMeetups.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                    .reduceRight { a, b -> a + b }
            assertThat(meetUps, hasItems(meetUp2.copy(uuid = id2), meetUp.copy(uuid = id)))
            assertThat(meetUps, not(hasItem(meetUp3.copy(uuid = id3))))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(id).delete().await()
            db.collection(MEETUP_COLL).document(id2).delete().await()
            db.collection(MEETUP_COLL).document(id3).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }

    }

    @Test
    fun getNonExistentMeetUpReturnsNull() = runTest {
        val fetchedMeetup = firebaseMeetUpService.getMeetUpData("DuGrandNimporteQuoi")
        assertThat(fetchedMeetup, nullValue())
    }

    @Test
    fun joinMeetUpJoinInDb() = runTest {
        val userId1 = firebaseProfileService.createProfile(user1)
        val userId2 = firebaseProfileService.createProfile(user2)
        assertThat(userId1, notNullValue())
        assertThat(userId1, `is`("userId"))
        assertThat(userId2, notNullValue())
        assertThat(userId2, `is`("userId2"))
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.joinMeetUp(
                it,
                userId2!!,
                meetUp.startDate,
                firebaseProfileService.fetchUserProfile(userId2)!!
            )
            assertThat(result, instanceOf(Response.Success::class.java))
            val meetUp = firebaseMeetUpService.getMeetUpData(it)
            assertThat(meetUp, notNullValue())
            assertThat(meetUp!!.participantsId, hasItem(userId2))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(userId1!!).delete().await()
            db.collection(PROFILE_COLL).document(userId2).delete().await()
        }
    }

    @Test
    fun leaveMeetUpLeaveInDb() = runTest {
        val userId1 = firebaseProfileService.createProfile(user1)
        val userId2 = firebaseProfileService.createProfile(user2)
        assertThat(userId1, notNullValue())
        assertThat(userId2, notNullValue())
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            firebaseMeetUpService.joinMeetUp(
                it,
                userId2!!,
                meetUp.startDate,
                firebaseProfileService.fetchUserProfile(userId2)!!
            )
            val result = firebaseMeetUpService.leaveMeetUp(it, userId2!!)
            assertThat(result, instanceOf(Response.Success::class.java))
            val meetUp = firebaseMeetUpService.getMeetUpData(it)
            assertThat(meetUp, notNullValue())
            assertThat(meetUp!!.participantsId, not(hasItem(userId2)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
            db.collection(PROFILE_COLL).document(userId1!!).delete().await()
        }
    }

    @Test
    fun joinAlreadyJoinedMeetUpReturnsSuccess() = runTest {

        val id2 = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.joinMeetUp(
                it,
                id2!!,
                meetUp.startDate,
                firebaseProfileService.fetchUserProfile(id2)!!
            )
            assertThat(result, instanceOf(Response.Success::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
        }
    }

    @Test
    fun joinMeetUpAfterItEndedReturnFailure() = runTest {
        firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        val id2 = firebaseProfileService.createProfile(user2)
        id!!.let {
            val dateAfter = Calendar.getInstance()
            dateAfter.time = meetUp.endDate.time
            dateAfter.add(Calendar.YEAR, 2)
            val result = firebaseMeetUpService.joinMeetUp(
                it,
                id2!!,
                dateAfter,
                firebaseProfileService.fetchUserProfile(id2)!!
            )
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
            db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
        }
    }

    @Test
    fun joinFullMeetUpReturnsFailure() = runTest {
        firebaseProfileService.createProfile(user1)
        val smallMeetUp = meetUp.copy(capacity = 1)
        val id = firebaseMeetUpService.createMeetUp(smallMeetUp)
        assertThat(id, notNullValue())
        val id2 = firebaseProfileService.createProfile(user2)
        id!!.let {
            val result = firebaseMeetUpService.joinMeetUp(
                it,
                id2!!,
                smallMeetUp.startDate,
                firebaseProfileService.fetchUserProfile(id2)!!
            )
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
            db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
        }
    }

    @Test
    fun leaveMeetUpYouCreatedReturnsFailure() = runTest {
        val id2 = firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.leaveMeetUp(it, id2!!)
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
        }
    }

    @Test
    fun joinNonExistingMeetUpReturnsFailure() = runTest {
        val id2 = firebaseProfileService.createProfile(user1)
        val result = firebaseMeetUpService.joinMeetUp(
            "UWU",
            id2!!,
            Calendar.getInstance(),
            firebaseProfileService.fetchUserProfile(id2)!!
        )
        assertThat(result, instanceOf(Response.Failure::class.java))
    }

    @Test
    fun leaveNonExistingMeetUpReturnsFailure() = runTest {
        val result = firebaseMeetUpService.leaveMeetUp("UWU", "Whatever")
        assertThat(result, instanceOf(Response.Failure::class.java))
    }

    @Test
    fun leaveMeetUpWithoutJoiningBeforeReturnsFailure() = runTest {
        firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val result = firebaseMeetUpService.leaveMeetUp(it, "MichelId")
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
        }
    }

    @Test
    fun joinMeetUpWithoutCheckingCriterionAgeFails() = runTest {
        firebaseProfileService.createProfile(user2)
        val seniorMeetUp = meetUp.copy(
            creatorId = "userId2",
            criterionAge = Pair(45, 66),
            participantsId = listOf("userId2")
        )
        val id = firebaseMeetUpService.createMeetUp(seniorMeetUp)
        assertThat(id, notNullValue())
        val id2 = firebaseProfileService.createProfile(user1)
        assertThat(id2, notNullValue())
        id!!.let {
            var result = firebaseMeetUpService.joinMeetUp(
                it,
                id2!!,
                seniorMeetUp.startDate,
                firebaseProfileService.fetchUserProfile(id2)!!
            )
            assertThat(result, instanceOf(Response.Failure::class.java))
            // Make sure to clean for next tests
            val birthday = Calendar.getInstance()
            birthday.set(1964, 8, 4)
            val id3 = firebaseProfileService.createProfile(user1.copy(birthday = birthday))
            assertThat(id3, notNullValue())
            result = firebaseMeetUpService.joinMeetUp(
                it,
                id3!!,
                seniorMeetUp.startDate,
                firebaseProfileService.fetchUserProfile(id3)!!
            )
            assertThat(result, instanceOf(Response.Success::class.java))

            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(id2).delete().await()
            db.collection(PROFILE_COLL).document(id3).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
        }
    }

    @Test
    fun joinMeetUpWithoutCheckingCriterionGenderFails() = runTest {
        firebaseProfileService.createProfile(user2)
        val virilMeetUp = meetUp.copy(
            creatorId = user2.uuid,
            criterionGender = CriterionGender.MALE,
            participantsId = listOf(user2.uuid)
        )
        val id = firebaseMeetUpService.createMeetUp(virilMeetUp)
        assertThat(id, notNullValue())
        val id2 = firebaseProfileService.createProfile(user1)
        assertThat(id2, notNullValue())
        id!!.let {
            var result = firebaseMeetUpService.joinMeetUp(
                it,
                id2!!,
                virilMeetUp.startDate,
                firebaseProfileService.fetchUserProfile(id2)!!
            )
            assertThat(result, instanceOf(Response.Failure::class.java))
            val id3 = firebaseProfileService.createProfile(user1.copy(gender = Gender.MALE))
            assertThat(id3, notNullValue())
            result = firebaseMeetUpService.joinMeetUp(
                it,
                id3!!,
                virilMeetUp.startDate,
                firebaseProfileService.fetchUserProfile(id3)!!
            )
            assertThat(result, instanceOf(Response.Success::class.java))


            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(id2).delete().await()
            db.collection(PROFILE_COLL).document(id3).delete().await()
            db.collection(PROFILE_COLL).document(user2.uuid).delete().await()
        }
    }

    @Test
    fun editNonExistingMeetUpReturnsNull() = runTest {
        val id = firebaseMeetUpService.editMeetUp("EncoreNimporteQuoi", meetUp)
        assertThat(id, nullValue())
    }

    @Test
    fun editNonExistingFieldReturnsNull() = runTest {
        firebaseProfileService.createProfile(user1)
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        assertThat(id, notNullValue())
        id!!.let {
            val idNull = firebaseMeetUpService.editMeetUp(it, "NotAField", "NotAValue")
            assertThat(idNull, nullValue())
            db.collection(MEETUP_COLL).document(it).delete().await()
            db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
        }
    }

    @Test
    fun getAllUserMeetUpWorksCorrectly() = runTest {
        firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(description = "baobab")
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        val userMeetUps = firebaseMeetUpService.getUserMeetups(user1.uuid).take(3).toList()
        assertThat(userMeetUps[0], `is`(instanceOf(Response.Loading::class.java)))
        userMeetUps.subList(1, userMeetUps.size - 1).forEach {
            assertThat(it, instanceOf(Response.Success::class.java))
        }
        val meetUps =
            userMeetUps.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                .reduceRight { a, b -> a + b }
        assertThat(
            meetUps,
            containsInAnyOrder(meetUp.copy(uuid = id!!), meetUp2.copy(uuid = id2!!))
        )
        db.collection(MEETUP_COLL).document(id).delete().await()
        db.collection(MEETUP_COLL).document(id2).delete().await()
        db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
    }

    @Test
    fun getUserMeetupAfterDateWorksCorrectly() = runTest {
        firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(startDate = Calendar.getInstance().apply { time = Date(4) },
            endDate = Calendar.getInstance().apply { time = Date(5) })
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        val userMeetUps = firebaseMeetUpService.getUserMeetups(
            user1.uuid,
            currentDate = Calendar.getInstance().apply { time = Date(3) }).take(2).toList()
        assertThat(userMeetUps[0], `is`(instanceOf(Response.Loading::class.java)))
        userMeetUps.subList(1, userMeetUps.size - 1).forEach {
            assertThat(it, instanceOf(Response.Success::class.java))
        }
        val meetUps =
            userMeetUps.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                .reduceRight { a, b -> a + b }
        assertThat(meetUps, containsInAnyOrder(meetUp2.copy(uuid = id2!!)))
        db.collection(MEETUP_COLL).document(id!!).delete().await()
        db.collection(MEETUP_COLL).document(id2).delete().await()
        db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
    }

    @Test
    fun getNonExistentUserMeetUpReturnFailure() = runTest {
        val failure = firebaseMeetUpService.getUserMeetups("awefsefs").take(2).toList()
        assertThat(failure[0], `is`(instanceOf(Response.Loading::class.java)))
        assertThat(failure[1], `is`(instanceOf(Response.Failure::class.java)))
    }

    @Test
    fun getAllMeetUpsWithDateWorks() = runTest {
        firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(startDate = Calendar.getInstance().apply { time = Date(4) },
            endDate = Calendar.getInstance().apply { time = Date(5) })
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        val userMeetUps =
            firebaseMeetUpService.getAllMeetUps(Calendar.getInstance().apply { time = Date(3) })
                .take(1).toList()
        val meetUps =
            userMeetUps
                .reduceRight { a, b -> a + b }
        assertThat(meetUps, containsInAnyOrder(meetUp2.copy(uuid = id2!!)))
        db.collection(MEETUP_COLL).document(id!!).delete().await()
        db.collection(MEETUP_COLL).document(id2).delete().await()
        db.collection(PROFILE_COLL).document(user1.uuid).delete().await()
    }

    @Test
    fun getMeetUpAroundLocationWithDateWorks() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(
            location = Location(4.0, 4.0),
            startDate = Calendar.getInstance().apply { time = Date(4) },
            endDate = Calendar.getInstance().apply { time = Date(5) }) // ~ 628km
        val meetUp3 = meetUp.copy(location = Location(4.1, 4.0)) // ~ 636km
        val id = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        val id3 = firebaseMeetUpService.createMeetUp(meetUp3)
        assertThat(id, notNullValue())
        assertThat(id2, notNullValue())
        assertThat(id3, notNullValue())
        if (id != null && id2 != null && id3 != null) {
            val fetchedMeetupsFlow =
                firebaseMeetUpService.getMeetUpsAroundLocation(
                    meetUp.location,
                    630.0,
                    currentDate = Calendar.getInstance().apply { time = Date(3) })
            val fetchedMeetups = fetchedMeetupsFlow.take(2).toList()
            assertThat(fetchedMeetups[0], instanceOf(Response.Loading::class.java))
            fetchedMeetups.subList(1, fetchedMeetups.size - 1).forEach {
                assertThat(it, instanceOf(Response.Success::class.java))
            }
            val meetUps =
                fetchedMeetups.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                    .reduceRight { a, b -> a + b }
            assertThat(meetUps, containsInAnyOrder(meetUp2.copy(uuid = id2)))
            // Make sure to clean for next tests
            db.collection(MEETUP_COLL).document(id).delete().await()
            db.collection(MEETUP_COLL).document(id2).delete().await()
            db.collection(MEETUP_COLL).document(id3).delete().await()
            db.collection(PROFILE_COLL).document(userId!!).delete().await()
        }
    }

    @Test
    fun getPalParticipatingMeetupWorks() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val meetUp2 = meetUp.copy(
            participantsId = listOf("userId2")) // ~ 628km
        val id1 = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        val fetchedMeetupsFlow =
            firebaseMeetUpService.getMeetUpsAroundLocation(
                meetUp.location,
                630.0,
                showParam = ShowParam.PAL_PARTCIPATING,
                profile = user1
            )
        val fetchedMeetups = fetchedMeetupsFlow.take(2).toList()
        assertThat(fetchedMeetups[0], instanceOf(Response.Loading::class.java))
        fetchedMeetups.subList(1, fetchedMeetups.size - 1).forEach {
            assertThat(it, instanceOf(Response.Success::class.java))
        }
        val meetUps =
            fetchedMeetups.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                .reduceRight { a, b -> a + b }
        assertThat(meetUps, containsInAnyOrder(meetUp2.copy(uuid = id2!!)))
        db.collection(MEETUP_COLL).document(id1!!).delete().await()
        db.collection(MEETUP_COLL).document(id2).delete().await()
        db.collection(PROFILE_COLL).document(userId!!).delete().await()
    }

    @Test
    fun getCreatorParticipatingMeetupWorks() = runTest {
        val userId = firebaseProfileService.createProfile(user1)
        val userId2 = firebaseProfileService.createProfile(user2)
        val meetUp2 = meetUp.copy(creatorId = userId2!!, name = "damdam") // ~ 628km
        val id1 = firebaseMeetUpService.createMeetUp(meetUp)
        val id2 = firebaseMeetUpService.createMeetUp(meetUp2)
        assertThat(id1, notNullValue())
        assertThat(id2, notNullValue())
        val fetchedMeetupsFlow =
            firebaseMeetUpService.getMeetUpsAroundLocation(
                meetUp.location,
                630.0,
                showParam = ShowParam.PAL_CREATOR,
                profile = user1
            )
        val fetchedMeetups = fetchedMeetupsFlow.take(2).toList()
        assertThat(fetchedMeetups[0], instanceOf(Response.Loading::class.java))
        fetchedMeetups.subList(1, fetchedMeetups.size - 1).forEach {
            assertThat(it, instanceOf(Response.Success::class.java))
        }
        val meetUps =
            fetchedMeetups.filterIsInstance<Response.Success<List<MeetUp>>>().map { it.data }
                .reduceRight { a, b -> a + b }
        assert(meetUps[0].creatorId == userId2)
        db.collection(MEETUP_COLL).document(id1!!).delete().await()
        db.collection(MEETUP_COLL).document(id2!!).delete().await()
        db.collection(PROFILE_COLL).document(userId!!).delete().await()
        db.collection(PROFILE_COLL).document(userId2).delete().await()
    }
}