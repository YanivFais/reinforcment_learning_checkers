/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

/* DO NOT EDIT THIS FILE - it is machine generated */
#include "jni.h"
/* Header for class NativeCheckers */

#ifndef _Included_NativeCheckers
#define _Included_NativeCheckers
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     checkers_library_NativeCheckers
 * Method:    setBoardSize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_setBoardSize
  (JNIEnv *, jclass, jint);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    setOnlineLearning
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_setOnlineLearning
  (JNIEnv *, jclass, jboolean);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    calculateMove
 * Signature: (IIJJJJJZZ)[I
 */
JNIEXPORT jintArray JNICALL Java_checkers_library_NativeCheckers_calculateMove
  (JNIEnv *, jclass, jint, jint, jlong, jlong, jlong, jlong, jboolean, jboolean);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    unDo
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_unDo
  (JNIEnv *, jclass, jint);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    reDo
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_reDo
  (JNIEnv *, jclass, jint);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    learn
 * Signature: (ILcheckers/game/Move;)V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_learn
  (JNIEnv *, jclass, jint, jintArray, jintArray);

/*
 * Class:     checkers_library_NativeCheckers
 * Method:    clearHistory
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_clearHistory
  (JNIEnv *, jclass);

#ifdef __cplusplus
}
#endif
#endif