package com.golfpvcc.martytracker.data.repository

import androidx.room.withTransaction
import com.golfpvcc.martytracker.data.local.db.MartyTrackerDatabase
import com.golfpvcc.martytracker.data.local.db.dao.RoutePointDao
import com.golfpvcc.martytracker.data.local.db.dao.WalkDao
import com.golfpvcc.martytracker.data.local.mapper.toDomain
import com.golfpvcc.martytracker.data.local.mapper.toEntity
import com.golfpvcc.martytracker.domain.model.Walk
import com.golfpvcc.martytracker.domain.repository.WalkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomWalkRepository(
    private val database: MartyTrackerDatabase,
    private val walkDao: WalkDao,
    private val routePointDao: RoutePointDao
) : WalkRepository {

    override suspend fun saveWalk(walk: Walk): Long = database.withTransaction {
        val walkId = walkDao.insertWalk(walk.toEntity())
        routePointDao.insertAll(walk.route.map { it.toEntity(walkId) })
        walkId
    }

    override fun getAllWalks(): Flow<List<Walk>> =
        walkDao.getAllWalksWithRoute().map { list -> list.map { it.toDomain() } }

    override suspend fun getWalkById(id: Long): Walk? =
        walkDao.getWalkWithRoute(id)?.toDomain()

    override suspend fun deleteWalk(id: Long) = walkDao.deleteWalk(id)
}
