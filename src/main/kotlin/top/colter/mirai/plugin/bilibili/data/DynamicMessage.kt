package top.colter.mirai.plugin.bilibili.data

import top.colter.bilibili.data.dynamic.BiliDynamic


class DynamicMessage (
    val dynamic: BiliDynamic,
    val receiver: Set<Receiver>,
)

/**
 *
 * BiliDynamic -> BiliMessage
 *
 * Map<template name, template>
 *
 * Map<template name, message>
 * Map<message, List<contact>>
 * Map<template name, List<contact>>
 *
 */