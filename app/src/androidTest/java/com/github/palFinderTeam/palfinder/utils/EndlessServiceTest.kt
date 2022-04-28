package com.github.palFinderTeam.palfinder.utils

import androidx.test.core.app.ApplicationProvider
import org.junit.Test

class EndlessServiceTest {
    @Test
    fun startTest(){
        EndlessService.scheduleJob(ApplicationProvider.getApplicationContext())
    }
}