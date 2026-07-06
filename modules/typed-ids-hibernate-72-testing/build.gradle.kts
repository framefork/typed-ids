plugins {
    id("org.framefork.build.library-internal")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-72")))

    api(libs.hibernate.orm.v72)
    api(libs.hypersistence.utils.hibernate71)

}
