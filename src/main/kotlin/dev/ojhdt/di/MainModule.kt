package dev.ojhdt.di

import dev.ojhdt.connection.ConnectionController
import dev.ojhdt.connection.FcmController
import org.koin.dsl.module

val mainModule = module {
    single<ConnectionController> {
        ConnectionController()
    }
}