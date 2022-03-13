package com.github.palFinderTeam.palfinder.meetups

import com.github.palFinderTeam.palfinder.utils.Location

class MockMeetUpRepository : MeetUpRepository {
    val db: HashMap<String, MeetUp> = hashMapOf()
    private var counter = 0

    override suspend fun getMeetUpData(meetUpId: String): MeetUp? {
        return db[meetUpId]
    }

    override suspend fun createMeetUp(newMeetUp: MeetUp): String? {
        val key = counter.toString()
        db[key] = newMeetUp
        counter.inc()
        return key
    }

    override suspend fun editMeetUp(meetUpId: String, field: String, value: Any): String? {
        TODO("Not yet implemented")
    }

    override suspend fun editMeetUp(meetUpId: String, meetUp: MeetUp): String? {
        return if (db.containsKey(meetUpId)) {
            db[meetUpId] = meetUp
            meetUpId
        } else {
            null
        }
    }

    override suspend fun getMeetUpsAroundLocation(location: Location): Array<MeetUp>? {
        TODO("Not yet implemented")
    }
}