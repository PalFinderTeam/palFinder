package com.github.palFinderTeam.palfinder.map


class MapsViewModelTest {

/*

    private val meetUpRepository = MockMeetUpRepository()
    private val profileService = MockProfileService()
    private var viewModel = MapListViewModel(meetUpRepository, profileService)

    private lateinit var meetup1: MeetUp
    private lateinit var meetup2: MeetUp
    private lateinit var meetup3: MeetUp
    private lateinit var meetup4: MeetUp

    private val mockMap = mock(GoogleMap::class.java)
    private var mockMapCameraPosition:CameraPosition = CameraPosition(LatLng(0.0, 0.0), 0f, 0f, 0f)
    private fun createMockMarker(options: MarkerOptions): Marker{
        val marker = mock(Marker::class.java)
        marker.position = options.position
        marker.title = options.title
        return marker
    }

    private fun addToDB(meetUp: MeetUp){
        meetUpRepository.db.put(meetUp.uuid, meetUp)
    }

    private fun setMap(map: GoogleMap){
        viewModel.map = map
        viewModel.mapReady = true
    }




    @Before
    fun init() {




        val date1 = mock(Calendar::class.java)
        Mockito.`when`(date1.timeInMillis).thenReturn(0)

        val date2 = mock(Calendar::class.java)
        Mockito.`when`(date2.timeInMillis).thenReturn(1)

        Mockito.`when`(mockMap.addMarker(any(MarkerOptions::class.java))).thenAnswer { invocation ->
            val options = invocation.getArgument<MarkerOptions>(0)
            val marker = createMockMarker(options)
            marker
        }

        Mockito.`when`(mockMap.cameraPosition).thenReturn(mockMapCameraPosition)
        //val mockContext = mock(Context::class.java)

        //MapsInitializer.initialize(mock(Context::class.java))



        meetup1 = MeetUp(
            "1",
            "user1",
            null,
            "meetUp1Name",
            "meetUp1Description",
            date1,
            date2,
            Location(0.0, 0.0),
            emptySet(),
            false,
            2,
            listOf("user2"),
            null,
            null
        )

        meetup2 = MeetUp(
            "2",
            "user2",
            null,
            "meetUp2Name",
            "meetUp2Description",
            date1,
            date2,
            Location(15.0, -15.0),
            emptySet(),
            false,
            2,
            listOf("user2"),
            null,
            null
        )

        meetup3 = MeetUp(
            "3",
            "user3",
            null,
            "meetUp3Name",
            "meetUp3Description",
            date1,
            date2,
            Location(-30.0, 45.0),
            emptySet(),
            false,
            4,
            listOf("user2"),
            null,
            null
        )

        meetup4 = MeetUp(
            "4",
            "user4",
            null,
            "meetUp4Name",
            "meetUp4Description",
            date1,
            date2,
            Location(69.0, 42.0),
            emptySet(),
            false,
            1337,
            listOf("user2"),
            null, null
        )
    }



    @Test
    fun clearMarkers(){


        setMap(mockMap)
        addToDB(meetup3)
        addToDB(meetup4)
        viewModel.refresh()
        viewModel.clearMarkers()
        Assert.assertEquals(null, viewModel.getMarker(meetup3.uuid))
        Assert.assertEquals(null, viewModel.getMarker(meetup4.uuid))
        meetUpRepository.clearDB()
    }

    @Test
    fun testRefresh(){
        setMap(mockMap)
        viewModel.mapReady = false

        addToDB(meetup1)
        addToDB(meetup2)
        viewModel.refresh()
        Assert.assertEquals(null, viewModel.getMarker(meetup1.uuid))
        Assert.assertEquals(null, viewModel.getMarker(meetup2.uuid))

        viewModel.mapReady = true

        val expMarker1 = mockMap.addMarker(MarkerOptions().position(LatLng(meetup3.location.latitude, meetup3.location.longitude)).title(meetup3.uuid))
        val expMarker2 = mockMap.addMarker(MarkerOptions().position(LatLng(meetup3.location.latitude, meetup3.location.longitude)).title(meetup3.uuid))
        viewModel.refresh()
        Assert.assertEquals(null, viewModel.getMarker(meetup1.uuid))
        Assert.assertEquals(null, viewModel.getMarker(meetup2.uuid))

        val actMarker1 = viewModel.getMarker(meetup1.uuid)
        val actMarker2 = viewModel.getMarker(meetup2.uuid)

        Assert.assertEquals(expMarker1?.position, actMarker1?.position)
        Assert.assertEquals(expMarker1?.title, actMarker1?.title)
        Assert.assertEquals(expMarker2?.position, actMarker2?.position)
        Assert.assertEquals(expMarker2?.title, actMarker2?.title)

        meetUpRepository.clearDB()

    }


    @Test
    fun testCameraStartingPosition(){
        viewModel.mapReady = false
        val pos = LatLng(77.0, 52.0)
        viewModel.setCameraPosition(pos)
        Assert.assertEquals(pos, viewModel.getCameraPosition())
    }


    @Test
    fun testZoomStartingPosition(){
        viewModel.mapReady = false
        val zoom = 18f
        viewModel.setZoom(zoom)
        Assert.assertEquals(zoom, viewModel.getZoom())
    }



    @Test
    fun testPositionZoomStartingPosition(){
        viewModel.mapReady = false
        val zoom = 18f
        viewModel.setZoom(zoom)
        val pos = LatLng(77.0, 52.0)
        viewModel.setCameraPosition(pos)
        Assert.assertEquals(pos, viewModel.getCameraPosition())
        Assert.assertEquals(zoom, viewModel.getZoom())
    }
*/
}