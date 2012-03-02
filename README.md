India Tablet
============

This is an Android application that targets the Aakash tablet:

* $35 Android tablet (subsidized price)
* Android 2.2 (Froyo)
* http://aakashtablet.org/

It uses Jake Wharton's [ActionBarSherlock][1] to bring an intuitive ICS style
interface down to the Froyo level. You can read more about the app's purpose
on [my website][4] and [here][5].

![Example Image][3]

Design
======

The main UI and control logic can be found in the main org.vt.indiatab package.
No SharedPreferences of any kind are kept - there isn't any need as of yet. All
data is in an [SQLiteDatabase][6]. Everything SQLite is in the org.vt.indiatab.data
package. There is one database and 3 tables:

* `groups`: Has 5 columns
	* `_id`: Unique row id
	* `name`: The name given to the group
	* `dues`: Amount due from each member each meeting
	* `fees`: Amount charged by the accountant each meeting
	* `rate`: The compound interest rate, expressed as $X / $100 / Meeting
* `meetings`: Has columns for each real and simluated meeting
* `members`: Has columns for member info as well as existing loans they may have

The current implementation allows for data collection. I imagine that much more
can be done on the visualization side.

Developed By
============

* [Weston Thayer][2]

License
=======

	IndiaTablet is an Android app that aids teaching microfinancing.

	Copyright (C) 2010 Weston Thayer
	 
	IndiaTablet is free software: you can redistribute it and/or modify it under the
	terms of the GNU General Public License as published by the Free Software
	Foundation, either version 3 of the License, or (at your option) any later
	version.
	 
	IndiaTablet is distributed in the hope that it will be useful, but WITHOUT ANY
	WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
	A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

	You should have received a copy of the GNU General Public License along with
	IndiaTablet.  If not, see <http://www.gnu.org/licenses/>.

	NOTE: all original images and audio are also Copyright by Weston Thayer 2010.
	Unauthorized use is not permitted.

 [1]: https://github.com/JakeWharton/ActionBarSherlock
 [2]: http://westonthayer.com
 [3]: http://westonthayer.com/images/india/screen.png
 [4]: http://westonthayer.com/projects/india/
 [5]: http://www.id4learning.com/bahikhaata.html
 [6]: http://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html