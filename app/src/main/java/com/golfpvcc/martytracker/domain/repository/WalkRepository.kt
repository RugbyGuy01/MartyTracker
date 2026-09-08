package com.golfpvcc.martytracker.domain.repository

import com.golfpvcc.martytracker.domain.model.Walk
import kotlinx.coroutines.flow.Flow

interface WalkRepository {
    suspend fun saveWalk(walk: Walk): Long
    fun getAllWalks(): Flow<List<Walk>>
    suspend fun getWalkById(id: Long): Walk?
    suspend fun deleteWalk(id: Long)
}
