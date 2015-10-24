# KnightWatch
An FRC scouting app for Android initially developed at FRC Team 296, currently maintained by Team 2706.
Maintaining mentor: Mike Ounsworth <ounsworth@gmail.com>


### TODO List ideas:

- get the Android Back button to work
- fix how it forgets files you've loaded
- Include other statistics to team page:
  - Defensive Power Rating: DPR is calculated just like OPR, except you use the opposing alliance's score instead of your own alliance's score.
  - Calculated Contribution to Winning Margin: CCWM is like OPR & DPR, but instead of the raw scores, you use the point difference (aka winning margin)
  - Projected Seed (based on simulating all future matches)
  - A button to show all matches that team played (including future match predictions, in a different colour)
- on home screen: show Tournament Seedings (current & predicted)
- Schedule search: given a team number, when is their next match?
  - possibly set system alarms w/ notifications for important matches?
- be able to auto-fetch the match data from somewhere on the internet
- use bluetooth to share data between devices runnig the app (so that as long as one phone in the cluster has internet, they all can get live match results / upload data)
- upload data to a server so that the drive team has live scouting data (possible through Google Drive API?)
- Team Photos:
  - is there a more effective way to load them into the app? Maybe also a Google Drive API?
  - More than one photo per team?
  - pinch zoom
