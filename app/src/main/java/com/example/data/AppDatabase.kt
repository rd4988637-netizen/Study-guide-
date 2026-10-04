package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.StudyDao
import com.example.data.model.StudySession
import com.example.data.model.StudySubject
import com.example.data.model.UserSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [StudySession::class, StudySubject::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyDao(): StudyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "study_circle_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default subjects and settings
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getInstance(context).studyDao()
                                dao.insertInitialSubjects(
                                    listOf(
                                        StudySubject(name = "Computer Science", colorHex = "#06B6D4", iconName = "code"),
                                        StudySubject(name = "Mathematics", colorHex = "#8B5CF6", iconName = "calculate"),
                                        StudySubject(name = "Literature & Writing", colorHex = "#F59E0B", iconName = "book"),
                                        StudySubject(name = "Languages", colorHex = "#10B981", iconName = "translate"),
                                        StudySubject(name = "Deep Focus Hub", colorHex = "#EC4899", iconName = "psychology")
                                    )
                                )
                                dao.saveUserSettings(UserSettings())
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
