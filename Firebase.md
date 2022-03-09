#  URL

https://console.firebase.google.com/u/1/project/palfinder-sdp/overview

# Database

## Collections

### User

surname (string)
name (string)
username (string)
join_date (timestamp)
(picture, explore best way to link picture to storage)

### Event

icon (ref, explore best way to link icon in storage)
name (string)
description (string)
location (geoposition)
time:
    start (timestamp)
    end (timestamp)
tags (array, must decide on proper data type)
capacity (max number of participant, number)
participants (array of user)


### Basic Activites name (dictionnary)

TBC
