/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
export interface JsonWriter {
    writeNumber(value: number): void;
    write(text: string): void;
    writeQuoted(text: string): void;
}