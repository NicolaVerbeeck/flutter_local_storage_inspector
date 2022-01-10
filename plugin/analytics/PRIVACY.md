# Analytics (opt-in)

To track plugin feature usage users can opt-in to send anonymous metrics to an analytics server (currently google analytics).

Users can easily change their opt-in status by going to the storage inspector preferences contained in the IDE preferences.

### What/why do we track
We do NOT track any data you view/edit, only metrics!

To increase your privacy, events are batched before they are sent, removing any time stamping. These
batched events are saved in a cache file. This file only contains the minimal data required to send,
not even the random user UUID is stored.

* Random user UUID
  * To identify user count
  * Truly random UUID
  * Saved in the IDE settings
  * Reset every time opt-in status toggles
* Rough location
  * Based off IP address
  * To better understand who is using the plugin
* Current language
  * Based of Locale.getCurrent()
  * To understand if we need to have more localization options
* Desktop OS Name
  * To understand our demographic
* Events, limited to a subset of the following. **Note** that no identifiable information is shared, only counts
  * Number of connection events
  * Key-value servers
    * Number of entries inspected
    * Number of times entry edited
    * Number of times entry added
    * Number of times entry removed
    * Number of times searched
  * File servers
    * Number of files inspected
    * Number of files downloaded
    * Number of files uploaded
    * Number of files deleted

### What we don't track
A non-exhaustive list of things we don't track:
* Your IP address
  * Anonymized
* Data you view/edit/download/...
* Project data
* Timestamps
  * We batch data to prevent leaking this information