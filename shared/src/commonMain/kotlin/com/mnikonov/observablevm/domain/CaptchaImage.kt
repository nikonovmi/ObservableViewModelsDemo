package com.mnikonov.observablevm.domain

enum class CaptchaImage(val type: CaptchaType?) {
    Bus1(CaptchaType.BUS),
    Bus2(CaptchaType.BUS),
    Bus3(CaptchaType.BUS),
    Bus4(CaptchaType.BUS),
    Guitar1(CaptchaType.GUITAR),
    Guitar2(CaptchaType.GUITAR),
    Guitar3(CaptchaType.GUITAR),
    Guitar4(CaptchaType.GUITAR),
    Dog1(CaptchaType.DOG),
    Dog2(CaptchaType.DOG),
    Dog3(CaptchaType.DOG),
    Dog4(CaptchaType.DOG),
    Random1(null),
    Random2(null),
    Random3(null),
    Random4(null),
    Random5(null),
    Random6(null),
}

enum class CaptchaType {
    BUS, GUITAR, DOG,
}
