/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

// definitions of bridge between c++ and java functions

#include <math.h>
#include <time.h>
#include <fstream>

#include <list>
#include <vector>
#include <iostream>
#include <bitset>
#include "NativeCheckers.h"
#include "GameBoard.h"
#include "Board.h"
#include "Game.h"
#include "Move.h"
#include "Player.h"
#include "Checkers.h"

using namespace std;


/**
 * calculate a move for Player
 * @param myColor moving Player 's color
 * @param myLevel level of Player
 * @param blackPeons coded representation of black peons on board
 * @param blackKings coded representation of black kings on board
 * @param whitePeons coded representation of white peons on board
 * @param whiteKings coded representation of white kings on board
 * @param addBoard indicator for adding board to history
 * @param useOpeningBook indicator for using opening book in play if possible
 */
JNIEXPORT jintArray JNICALL Java_checkers_library_NativeCheckers_calculateMove
  (JNIEnv * env, jclass cl, jint myColor, jint myLevel,
	jlong blackPeons, jlong blackKings, jlong whitePeons, jlong whiteKings,
	jboolean addBoard,jboolean useOpeningBook)
{
	Move* mv;
	Game* game = Game::getInstance();
	game->clearReDo();
	int bits = game->getBoardSize();
	Player *player = game->getPlayer(myColor);
	// create board in size and set it as the current board
	switch (bits)
	{
	case 32: // 8x8
		{
			ParamBoard<32> b(whitePeons,blackPeons,whiteKings,blackKings);
			game->setCurrentBoard(&b);
			if (addBoard)
			{
				ParamBoard<32>* board = new ParamBoard<32>(b);
				game->pushToHistory(board);
			}
			break;
		}
	case 18: // 6x6
		{
			ParamBoard<18> b(whitePeons,blackPeons,whiteKings,blackKings);
			game->setCurrentBoard(&b);
			if (addBoard)
			{
				ParamBoard<18>* board = new ParamBoard<18>(b);
				game->pushToHistory(board);
			}
			break;
		}
	case 50: // 10x10
		{
			// STL bitset can't handle __int64
			bitset<50> blackP = (blackPeons) >> 32;
			bitset<50> blackK = (blackKings) >> 32;
			bitset<50> whiteP = (whitePeons) >> 32;
			bitset<50> whiteK = (whiteKings) >> 32;
			blackP <<= 32;
			blackK <<= 32;
			whiteP <<= 32;
			whiteK <<= 32;
			blackP |= (long)blackPeons;
			blackK |= (long)blackKings;
			whiteP |= (long)whitePeons;
			whiteK |= (long)whiteKings;
			ParamBoard<50> b(whiteP,blackP,whiteK,blackK);
			game->setCurrentBoard(&b);
			if (addBoard)
			{
				ParamBoard<50>* board = new ParamBoard<50>(b);
				game->pushToHistory(board);
			}
			break;
		}
	}
	// set level
	player->setLevel((char) myLevel);

	int val = 0;
	
	if (Game::isOnlineLearningActive())
	{
		if (useOpeningBook)
			mv = player->playByBook(&val); // playing by book
 		else
			mv = player->play(&val); // playing using min-max tree
	}
	else // If online learning is not used, no need to keep values
	{
		if (useOpeningBook)
			mv = player->playByBook(); // playing by book
 		else
			mv = player->play(); // playing using min-max tree
	}

	if (!mv)
		return 0; // there are no moves, I lose..

	// add to history for undo and draw check
	game->pushToHistory(GameBoard::makeMove(Game::getInstance()->getCurrentBoard(),mv,myColor));

	// add all the move's hits
	int len = mv->hops.size();
	jintArray arr = env->NewIntArray(len);
	int i=0;
	list<int>::iterator it = mv->hops.begin();
	jint* buf = new jint[len];
	for (; it != mv->hops.end(); it++, i++)
	{
		buf[i] = *it;
	}

	// set the last  move
	switch (myColor)
	{
	case COLOR_WHITE:
		if (!useOpeningBook || GameBoard::getStage(game->getCurrentBoard()) < STAGES - 1)
			game->setLastWhiteMove(mv, val);
		else
		{
			game->setLastWhiteMove(0, val);
			delete mv;
		}
		break;
	case COLOR_BLACK:
		if (!useOpeningBook || GameBoard::getStage(game->getCurrentBoard()) < STAGES - 1)
			game->setLastBlackMove(mv, val);
		else
		{
			game->setLastBlackMove(0, val);
			delete mv;
		}
		break;
	}

	env->SetIntArrayRegion(arr, 0, len, buf);
	delete buf;
	return arr;
}


/**
 * undo moves
 * @param size number of undo moves
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_unDo
  (JNIEnv *env, jclass cl, jint size)
{
	Game::getInstance()->unDo(size);
}

/**
 * redo un-did moves
 * @param size number of redo moves
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_reDo
  (JNIEnv *env, jclass cl, jint size)
{
	Game::getInstance()->reDo(size);
}

/**
 * clear history of board
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_clearHistory
  (JNIEnv *env, jclass cl)
{
	Game::getInstance()->clearHistory();
}

/**
 * set used board size
 * @param size size of board as number of possible squares used on board
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_setBoardSize
  (JNIEnv *env, jclass cl, jint size)
{
	Game::getInstance()->setBoardSize(size * size / 2);

	//read weights for this board size
	char fname[50];
	
	sprintf(fname, "%s%s%s%d%s", DATA_DIRECTORY,DIR_DELIMITER,WEIGHTS_FILE_PREFIX_NAME, 
		size*size/2,WEIGHTS_FILE_EXTENSION);
	vector< vector<unsigned short> > weights(STAGES);
	ifstream ifs;
	ifs.open(fname);
	if (!ifs.is_open())
	{
		cerr << "Error:unable to open weights file " << fname << endl;
		return;
	}
	char s[50];
	for (int i=0; i<STAGES; i++)
	{
		vector<unsigned short> stageWeights(PARAMS_NUM);
		for (int j=0; j<PARAMS_NUM; j++)
		{
			ifs.getline(s,50);
			stageWeights[j] = Game::getInstance()->scaleWeight(atof(s));
		}
		weights[i] = stageWeights;
	}

	//set player's weights
	Game::getInstance()->getPlayer(COLOR_WHITE)->setWeights(weights);
	Game::getInstance()->getPlayer(COLOR_BLACK)->setWeights(weights);
	ifs.close();
}


/**
 * learn Player using given move
 * @param color learner color
 * @param oppMoveHops opponent move's hops
 * @param oppMoveHits opponent move's hits
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_learn
  (JNIEnv *env, jclass cl, jint color, jintArray oppMoveHops, jintArray oppMoveHits)
{
	Game* game = Game::getInstance();
	if (!game->onlineLearningPermitted())
		return;
	Move* playerMove = 0;
	int val;

	switch (color)
	{
	case COLOR_WHITE:
		playerMove = game->getLastWhiteMove();
		val = game->getLastWhiteMoveValue();
		break;
	case COLOR_BLACK:
		playerMove = game->getLastBlackMove();
		val = game->getLastBlackMoveValue();
		break;
	}
	// only if have last move (not first move)
	if (playerMove)
	{
		// decode move
		Move oppMove;
		int len = env->GetArrayLength(oppMoveHops);
		jint* buf = new jint[len];
		env->GetIntArrayRegion(oppMoveHops, 0, len, buf);
		for (int i=0; i<len; ++i)
			oppMove.hops.push_back(buf[i]);
		delete buf;
		len = env->GetArrayLength(oppMoveHits);
		buf = new jint[len];
		env->GetIntArrayRegion(oppMoveHits, 0, len, buf);
		for (i=0; i<len; ++i)
			oppMove.hits.push_back(buf[i]);
		delete buf;

		Board* board = game->getPreviousBoard();
		if (!board)
			return;

		// learn the player using board,player's last move,expected value
		//and opponent move using alpha factor of Q-learning
		game->getPlayer(color)->learn(board, playerMove, val, &oppMove, 0.005);


		int oppColor = opp(color);
		game->getPlayer(oppColor)->setWeights(game->getPlayer(color)->getWeights());


		// write weights to file
		ofstream of;
		char name[100];
		sprintf(name, "%s%s%s%d%s", DATA_DIRECTORY,DIR_DELIMITER,WEIGHTS_FILE_PREFIX_NAME, 
			game->getBoardSize(),WEIGHTS_FILE_EXTENSION);
		of.open(name);
		if (!of.is_open())
		{
			cerr << "Error:unable to open weights file " << name << endl;
			return;
		}
		for (i=0; i<STAGES; i++)
		{
			for (int j=0; j<PARAMS_NUM; j++)
				of << game->scaleWeight(game->getPlayer(color)->getWeights()[i][j]) << "\n";
		}
		of.close();
	}
}

/**
 * Toggles online learning
 */
JNIEXPORT void JNICALL Java_checkers_library_NativeCheckers_setOnlineLearning
  (JNIEnv *env, jclass cl, jboolean active)
{	
	Game::setOnlineLearningActive(active != 0);
}
