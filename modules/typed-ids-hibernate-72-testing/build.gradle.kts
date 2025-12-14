plugins {
    id("framefork.java")
}

dependencies {
    api(project(":typed-ids-testing"))
    api(testFixtures(project(":typed-ids-hibernate-72")))

    api(libs.hibernate.orm.v72)
    api(libs.hypersistence.utils.hibernate71)
}
