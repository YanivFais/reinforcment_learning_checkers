/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#ifndef __CHECKERS_DEFS_H
#define __CHECKERS_DEFS_H

/**
 * global definitions for checkers project
 */

/** 
 * directory of data file
 */
#define DATA_DIRECTORY "data"

/**
 * delimiter used for directories
 */
#define DIR_DELIMITER "\\"

/**
 * prefix name of weights file
 **/
#define WEIGHTS_FILE_PREFIX_NAME "weights"

/**
 * prefix name of book file
 */
#define BOOK_FILE_PREFIX_NAME "book"

/**
 * extension for weight file
 */
#define WEIGHTS_FILE_EXTENSION ".dat"

/**
 * extension for book file
 */
#define BOOK_FILE_EXTENSION ".dat"


/**
 * threshold for weight, below this constant parameters is considered insignificant and
 * would not be calculated in order to save time
 */
#define WEIGHT_THRESHOLD 5

/**
 * threshold for weight as double,
 * below this constant parameters is considered insignificant and
 * would not be calculated in order to save time
 */
#define WEIGHT_THRESHOLD_DBL 0.001

/**
 * black Player color
 */
#define COLOR_BLACK 2

/**
 * white Player color
 */
#define COLOR_WHITE 1

/**
 * opponent color
 */
#define opp(color) ((color == COLOR_WHITE)? COLOR_BLACK : COLOR_WHITE)

/**
 * number of parameters
 */
#define PARAMS_NUM		20

/**
 * number of stages
 */
#define STAGES			6

/**
 * minimum finder
 */
#ifndef min
	#define min(x,y) ((x<y)? (x) : (y))
#endif

#endif