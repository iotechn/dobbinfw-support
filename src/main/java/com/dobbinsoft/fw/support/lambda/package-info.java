package com.dobbinsoft.fw.support.lambda;

/**
 * 这个包主要是为了实现，接口行为参数化。
 *
 * RPC时，将一个回调接口封装为行为参数传递给上游系统。上游系统处理完后，直接调用传递过去的接口，也就是接口作为参数传递
 * 下游系统RPC时，需要待上回调的地址，方法坐标，以及签名，行为参数过期时间。
 *
 * 目前业务系统主要应用场景，Promall -> Agent 时，会存在Function Call。Agent 需要调用Promall的Function。
 */