package com.phonecall.reminder.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.phonecall.reminder.service.CallService

class CallActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_ANSWER = "com.phonecall.reminder.ACTION_ANSWER"
        const val ACTION_DECLINE = "com.phonecall.reminder.ACTION_DECLINE"
        const val ACTION_END_CALL = "com.phonecall.reminder.ACTION_END_CALL"
        const val ACTION_SNOOZE = "com.phonecall.reminder.ACTION_SNOOZE"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val serviceIntent = Intent(context, CallService::class.java)
        
        when (intent.action) {
            ACTION_ANSWER -> {
                serviceIntent.action = CallService.ACTION_ANSWER_CALL
            }
            ACTION_DECLINE, ACTION_END_CALL -> {
                serviceIntent.action = CallService.ACTION_END_CALL
            }
            ACTION_SNOOZE -> {
                serviceIntent.action = CallService.ACTION_SNOOZE
                serviceIntent.putExtra(
                    CallService.EXTRA_SNOOZE_MINUTES,
                    intent.getIntExtra(CallService.EXTRA_SNOOZE_MINUTES, 5)
                )
            }
            else -> return
        }
        
        context.startService(serviceIntent)
    }
}
