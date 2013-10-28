CBOR
====

A C# and Java implementation of Concise Binary Object Representation (RFC 7049).  
CBOR is a general-purpose binary data format designed by Carsten 
Bormann, and its data model "is an extended version of the JSON data model",
supporting many more types of data than JSON. "CBOR was inspired by 
MessagePack", but "is not intended as a version of or replacement for 
MessagePack."

Documentation
------------

This library defines one class, called CBORObject, that allows you to read and
write CBOR objects to and from data streams and byte arrays, and to convert JSON
text to CBOR objects.  It defines the following methods.

Note:  The methods below are included in the C# version of this library.  The Java
version was translated from the original C#, and some of the methods shown here
may be missing from the Java version.

- AsBigInteger(), AsBoolean(), AsByte(), AsDouble(), AsInt16(), AsInt32(),
  AsInt64(), AsSByte(), AsSingle(), AsString(), AsUInt16, AsUInt32(), AsUInt64():
  Converts the CBOR object to different data types.  Throws OverflowException if
  the value is out of range, or InvalidOperationException if the object's type is not
  appropriate for conversion.
- Equals(object other): Compares two CBORObject objects for equality.
- GetHashCode(): Calculates the hash code of a CBORObject object.
- CBORObject.FromHashBytes(object): Generates a CBOR object from CBOR binary
   data.  Throws FormatException if not all of the bytes represent a CBOR object.
- CBORObject.FromJSONString(string): Generates a CBOR object from JSON text.
- CBORObject.FromObject(object): Generates a CBOR object from an arbitrary
  object.  Not all objects are supported; for unsupported types, throws
  ArgumentException.
- CBORObject.FromObject(object, int/ulong): Generates a CBOR object from an arbitrary
  object and tags it with a number defined in the CBOR specification and [IANA
  registry](http://www.iana.org/assignments/cbor-tags/cbor-tags.xhtml).
- CBORObject.Read(System.IO.Stream): Generates a CBOR object by reading it
  from a byte stream.
- Add(CBORObject):  Adds a CBOR object to this array.
- Add(CBORObject, CBORObject):  Adds a CBOR object to this map. 
  Throws ArgumentException if the key already exists.
- ContainsKey(CBORObject): Returns true if the key exists in this map; otherwise, false.
- ToBytes(): Converts the CBOR object to its binary representation.
- ToString(): Returns this CBOR object in string form.
   The format is intended to be human-readable, not machine-
   parsable, and the format may change at any time.
- ToJSONString() - Converts this object to a JSON string.  This function
    not only accepts arrays and maps (the only proper
		JSON objects under RFC 4627), but also integers,
		strings, byte arrays, and other JSON data types.
- WriteTo(System.IO.Stream): Writes this CBOR object to a data stream.
- Tag - Returns this object's tag, or 0 if it has no tag.
- obj[int] - Gets or sets a value in this CBOR array. Valid only for arrays.
- CBORObject.Write(object, System.IO.Stream) - Writes an arbitrary object in
  the CBOR format to a data stream.  Not all objects are supported; for 
  unsupported types, throws ArgumentException.
- CBORObject.Break - CBOR object for the break value.
- CBORObject.Null - CBOR object for null.
- CBORObject.True - CBOR object for true.
- CBORObject.False - CBOR object for false.
- CBORObject.Undefined - CBOR object for an undefined value.
- CBORObject.Count - Gets the length of this CBOR object. Valid only for arrays
  and maps.
- CBORObject.IsBreak - Returns true if this is the break value.
- CBORObject.IsNull - Returns true if this is null.
- CBORObject.IsTrue - Returns true if this is true.
- CBORObject.IsFalse - Returns true if this is false.
- CBORObject.IsUndefined - Returns true if this is the undefined value.
- CBORObject.IsTagged - Returns true if this object has a tag.

About
-----------

Written in 2013 by Peter O.

Any copyright is dedicated to the Public Domain.
http://creativecommons.org/publicdomain/zero/1.0/

If you like this, you should donate to Peter O.
at: http://upokecenter.com/d/