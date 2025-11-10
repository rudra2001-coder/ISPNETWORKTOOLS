package com.rudra.ispnetworktools.di






import android.content.Context
import androidx.room.Room
import com.rudra.ispnetworktools.data.AppDatabase
import com.rudra.ispnetworktools.data.TestResultDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "isp_network_tools.db"
        ).build()
    }

    @Provides
    fun provideTestResultDao(database: AppDatabase): TestResultDao {
        return database.testResultDao()
    }
}