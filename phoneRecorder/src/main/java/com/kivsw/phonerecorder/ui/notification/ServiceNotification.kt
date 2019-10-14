package com.kivsw.phonerecorder.ui.notification

import android.content.Context
import com.kivsw.phonerecorder.model.settings.ISettings

class ServiceNotification(context:Context, settings: ISettings, notificationId:Int):AntiTaskKillerNotification(context, settings, notificationId)
