﻿## PeterO.Cbor.ICBORConverter<T>

    public abstract interface ICBORConverter`1<T>

Interface implemented by classes that convert objects of arbitrary types to CBOR objects.

### ToCBORObject

    public abstract virtual PeterO.Cbor.CBORObject ToCBORObject(
        T obj);

Converts an object to a CBOR object.

<b>Parameters:</b>

 * <i>obj</i>: An object to convert to a CBOR object.

<b>Returns:</b>

A CBOR object.

