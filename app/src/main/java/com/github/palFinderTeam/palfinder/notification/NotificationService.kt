package com.github.palFinderTeam.palfinder.notification

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import com.github.palFinderTeam.palfinder.R
import com.github.palFinderTeam.palfinder.cache.DictionaryCache
import com.github.palFinderTeam.palfinder.chat.CHAT
import com.github.palFinderTeam.palfinder.chat.CachedChatService
import com.github.palFinderTeam.palfinder.chat.ChatActivity
import com.github.palFinderTeam.palfinder.chat.ChatService
import com.github.palFinderTeam.palfinder.di.FirestoreModule
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.CachedMeetUpService
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.FirebaseMeetUpService
import com.github.palFinderTeam.palfinder.meetups.meetupRepository.MeetUpRepository
import com.github.palFinderTeam.palfinder.meetups.meetupView.MEETUP_SHOWN
import com.github.palFinderTeam.palfinder.meetups.meetupView.MeetUpView
import com.github.palFinderTeam.palfinder.profile.*
import com.github.palFinderTeam.palfinder.profile.profile.ProfileActivity
import com.github.palFinderTeam.palfinder.profile.services.CachedProfileService
import com.github.palFinderTeam.palfinder.profile.services.FirebaseProfileService
import com.github.palFinderTeam.palfinder.profile.services.ProfileService
import com.github.palFinderTeam.palfinder.utils.EndlessService
import com.github.palFinderTeam.palfinder.utils.context.AppContextService
import com.github.palFinderTeam.palfinder.utils.context.ContextService
import com.github.palFinderTeam.palfinder.utils.time.RealTimeService
import com.github.palFinderTeam.palfinder.utils.time.TimeService
import com.github.palFinderTeam.palfinder.utils.time.isBefore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


class NotificationService @Inject constructor(
    val contextService: ContextService,
    val timeService: TimeService,
    private val meetupService: MeetUpRepository,
    val profileService: ProfileService,
    val chatService: ChatService
): JobService() {

    // Android want a default constructor
    @Suppress("unused")
    constructor():this(
        AppContextService(),
        RealTimeService(),
        CachedMeetUpService(FirebaseMeetUpService(FirestoreModule.provideFirestore(),
            FirebaseProfileService(FirestoreModule.provideFirestore())
        ), RealTimeService(), AppContextService()),
        CachedProfileService(FirebaseProfileService(FirestoreModule.provideFirestore()), RealTimeService(), AppContextService()),
        CachedChatService(FirestoreModule.provideFirestore(), AppContextService()),
    )

    private val notifications = DictionaryCache("notification", CachedNotification::class.java, false, contextService.get())
    private val meetupsMetaData = DictionaryCache("meetup_meta", MeetupMetaData::class.java, false, contextService.get())
    private val profileMetaData = DictionaryCache("profile_meta", ProfileMetaData::class.java, false, contextService.get())
    private val achievementMetaData = DictionaryCache("achievement_meta", AchievementMetaData::class.java, false, contextService.get())

    fun action(){
        for (notif in notifications.getAll()){
            if (notif.time.isBefore(timeService.now())){
                NotificationHandler(contextService.get()).post(notif.title, notif.content, notif.icon)
                notifications.delete(notif.uuid)
            }
        }
        val context = contextService.get()
        CoroutineScope(Dispatchers.Main).launch {
            // Notification For Follow
            val id = profileService.getLoggedInUserID()
            var loggedUser: ProfileUser? = null
            if (id != null){
                loggedUser = profileService.fetch(id)
                if (loggedUser != null){
                    for(p in loggedUser.followed){
                        val user = profileService.fetch(p)
                        if (!profileMetaData.contains(p) && user != null){
                            profileMetaData.store(p, ProfileMetaData(p, true))
                            val intent = Intent(context, ProfileActivity::class.java).apply {
                                putExtra(USER_ID, user.uuid)
                            }
                            NotificationHandler(context).post(context.getString(R.string.following_title), context.getString(R.string.following_content).format(user.username), R.drawable.icon_beer, intent)
                        }
                    }
                    for (a in Achievement.values()) {
                        if (a in loggedUser.achievements() && !achievementMetaData.contains(a.toString())){
                            achievementMetaData.store(a.toString(), AchievementMetaData(a.toString(), true))
                            NotificationHandler(context).post(context.getString(R.string.achievement_title), context.getString(R.string.achievement_content).format(a.aName), R.drawable.icon_beer)
                        }
                    }
                }
            }
            //Notification For Meetup
            for (m in meetupService.getAllJoinedMeetupID()) {
                val meetup = meetupService.fetch(m)
                val metadata = if (meetupsMetaData.contains(m)) {
                    meetupsMetaData.get(m)
                } else {
                    val ret = MeetupMetaData(m, false, "", mutableListOf())
                    meetupsMetaData.store(m, ret)
                    ret
                }
                if (meetup != null) {
                    if (meetup.creatorId == id) {
                        val data = meetupsMetaData.get(m)
                        val news = meetup.participantsId.subtract(data.participant.toSet()).filter { it != id }
                        data.participant.addAll(news)
                        meetupsMetaData.store(m, data)
                        
                        if (news.isNotEmpty()) {
                            val names = profileService.fetch(news.toList())!!.map { it.name }
                                .reduce { x, y -> "$x, $y" }
                            data.participant.addAll(news)
                            meetupsMetaData.store(m, data)

                            if (!loggedUser!!.isMeetupMuted(m)) {
                                val intent = Intent(context, ProfileActivity::class.java).apply {
                                    putExtra(USER_ID, news[0])
                                }
                                NotificationHandler(context).post(
                                    meetup.name,
                                    context.getString(R.string.meetup_new_participant)
                                        .format(names),
                                    R.drawable.icon_beer,
                                    intent
                                )
                            }
                        }
                    }

                    // Check if meetup started and notification not sent
                    if (!metadata.sendStartNotification && meetup.startDate.isBefore(timeService.now())) {
                        val intent = Intent(context, MeetUpView::class.java).apply {
                            putExtra(MEETUP_SHOWN, m)
                        }
                        if (!loggedUser!!.isMeetupMuted(m)){
                            NotificationHandler(context).post(meetup.name, meetup.description, R.drawable.icon_beer, intent)
                        }
                        metadata.sendStartNotification = true
                        meetupsMetaData.store(m, metadata)
                    }

                    // Look for message of that meetup
                    val messages = chatService.fetchMessages(m)
                    if (messages!= null && messages.isNotEmpty()) {
                        val meta = meetupsMetaData.get(m)
                        val last = messages.takeLast(1)[0]
                        val hash = last.hashCode().toString()

                        if (hash != meta.lastMessageNotification && last.sentBy != profileService.getLoggedInUserID() && loggedUser != null && !loggedUser.isMeetupMuted(m)) {
                            val name = profileService.fetch(last.sentBy)?.username?:""
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                putExtra(CHAT, m)
                            }
                            if (ChatActivity.currentlyViewChat != m) {
                                NotificationHandler(context).post(name, last.content, R.drawable.icon_beer, intent)
                            }
                            meta.lastMessageNotification = hash
                            meetupsMetaData.store(m, meta)
                        }
                    }
                }
            }
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        action()
        EndlessService.scheduleJob(applicationContext) // reschedule the job
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    data class MeetupMetaData(var uuid: String, var sendStartNotification: Boolean, var lastMessageNotification: String, var participant: MutableList<String> = mutableListOf())
    data class ProfileMetaData(var uuid: String, var sendStartNotification: Boolean)
    data class AchievementMetaData(var uuid: String, var sendStartNotification: Boolean)
}
