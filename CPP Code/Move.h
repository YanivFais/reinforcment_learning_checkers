/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#ifndef __MOVE__H
#define __MOVE__H

#include <list>
#include <iostream>
using namespace std;


/**
 * A move in the game
 */
struct Move
{
	/**
	 * hops in the way of move
	 */
	list<int> hops;

	/**
	 * hits in the way of move
	 */
	list<int> hits;
};

#endif // __MOVE__H