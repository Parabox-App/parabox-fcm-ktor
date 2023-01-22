package dev.ojhdt.di

import dev.ojhdt.connection.ConnectionController
import org.koin.dsl.module

val mainModule = module {
    single<ConnectionController> {
        ConnectionController()
    }
}