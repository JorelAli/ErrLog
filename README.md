# ErrLog
A bukkit plugin which helps log errors in console. Compatible with Spigot 1.12/1.12.1 (NOT compatible with Craftbukkit)

Commands
--------
* /errlog - Toggles error log viewing
* /errlog on - Turns error log viewing on
* /errlog off - Turns error log viewing off
* /errview <errorID> - Views an error log (This command should not be used manually, it's used within the plugin)
* /errlogs - Views a list of error logs since the plugin has been enabled (generally, since restart)

Permissions
-----------
* errlog.use - Accesses all features

Usage
-----
When /errlog is enabled, errors in the console will appear in chat. 

![alt text](https://raw.githubusercontent.com/Skepter/ErrLog/master/images/chatDisplay.PNG "")

These errors can be clicked (click the error message in chat) and it will open the GUI for information about the error.

#### Plugin Details
This shows information about the plugins which may have caused the error. It includes the plugin name, version and authors. 

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/plugindetails.PNG "")

#### Upload to Hastebin
Uploads the error log to [hastebin](http://hastebin.com) (read more about hastebin [here](https://hastebin.com/about)). This will basically upload the error log onto hastebin and output the generated link in chat (for you only). Hastebin is useful for temporarily storing logs as they are deleted automatically after a few days. 

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/hastebin.PNG "")

#### Error details (for developers)
Shows the stacktrace of the error. "Important" classes are highlighted in green (important classes are often the classes responsible for the error. Bukkit/Java/Sun classes are shown in gray)

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/biglog.PNG "")

#### Simplified error details (for developers)
Shows the stacktrace of the error. "Important" classes are highlighted in green. Other classes (Bukkit/Java/Sun) are not shown.

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/smalllog.PNG "")

#### Save error log to file
Stores the stacktrace in a file. The file location is displayed in chat, and is normally stored under /plugins/ErrLog/error<ID>.txt

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/savelog.PNG "")

#### Error information (for non-developers)
Gives a simple explanation of the error for people who don't understand code/stacktraces. It explains what the exception means (e.g. what a NullPointerException means), what the name of the function which called the code is and the plugin responsible. If the error was caused from an event, it will say the name of the event instead (e.g. BlockBreakEvent)

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/nodevlog.PNG "")

#### Error time
Displays the time when the error occured (in GMT)

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/time.PNG "")

#### Error ID
An error ID used by the plugin to keep track of the error internally (ignore this)

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/errid.PNG "")

#### Close
Closes the inventory

![alt text](https://github.com/Skepter/ErrLog/blob/master/images/close.PNG "")

Installation
------------
Download the latest version from [here](https://github.com/Skepter/ErrLog/releases) and drop into your /plugins/ folder. Reload/restart the server. (Compatible with plugman/plugin loaders)
