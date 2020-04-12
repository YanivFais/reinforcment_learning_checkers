/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/
#ifndef __GAME_H
#define __GAME_H

#include <list>
#include <limits>
#include "Move.h"
using namespace std;

class Player;
class Board;
class Book;

/**
 * representation of a singelton game
 */
class Game
{
private:
   /**
	* construct a new Game,private to implement singleton pattern
	*/
	Game();

	/**
	 * book for good known moves
	 */
	Book *book;

	/**
	 * number of bits used to represent board
	 */
	int _bits;

	/**
     * players
	 */
	Player *black, *white;

	/**
	 * board of game
	 */
	Board* _board;

	/**
	 * last boards in the game for checking draw and un-do option
	 */
	list<Board*> _boardsHistory;

	/**
	 * boards that were undo'd,for re-do option
	 */
	list<Board*> _boardsFuture;

	/**
	 * last move of black Player
	 */
	Move* _lastBlackMove;

	/**
	 * last move of white Player
	 */
	Move* _lastWhiteMove;

	/**
	 * value of last black Player's Move
	 **/
	int _lastBlackMoveValue;

	/**
	 * value of last white Player's Move
	 **/
	int _lastWhiteMoveValue;

	/**
	 * indicator for allowing "online learning" to be performed
	 */ 
	bool _allowOnlineLearning;

	/**
	 * indicates if online learning is active
	 * used to improve minmax performance.
	 */ 
	static bool _onlineLearningActive;

	/**
	 * is it a learning game
	 */
	bool _learning;

	/**
	 * one and only instance of Game
	 */
	static Game* instance;
public:


	/**
	 * @return instance of the one and only Game
	 */
	static inline Game* getInstance()
	{
		if (!instance) 
			instance = new Game();
		return instance;
	}

	/**
	* add a board to history
	* @param board board for add
	*/
	void pushToHistory(Board * board);

	/**
	* pop from boards history
	*/
	void popFromHistory();

	/**
	* check for "draw" situation using board and history
	* @param board current board to test history with
	*/
	bool checkDraw(Board* board) const;

	/**
	* un do a move, remove from history for "draw" check
	* @param size size of undo
	*/
	void unDo(unsigned size);

	/**
	* re do previuost un-did move
	* @param size size of re-do
	*/
	void reDo(unsigned size);

	/**
	* clear history of game,used for Game load
	*/
	void clearHistory();

	/**
	* clear re do moves,used when performing a move thus re-do is discarded
	*/
	void clearReDo();

	/**
	* set the game board's size
	* @param bits number of bits used to store each set of player (white peons & etc.)
	*/
	void setBoardSize(int bits);

	/**
	 * @return number of bits used to store board element
	 */
	int getBoardSize() const
	{
		return _bits;
	}

	/**
	* @param color reuqested color of Player
	* @return Player in color
	*/
	Player* getPlayer(int color);

	/**
	 * @return current Board played with
	 */
	Board* getCurrentBoard()
	{
		return _board;
	}

	/**
	 * set the current board of game
	 * @param board to be current board of game
	 */
	void setCurrentBoard(Board* board)
	{
		_board = board;
	}

	/**
	 * @return book used for knowsn moves
	 */
	Book* getBook()
	{
		return book;
	}

	/**
	 * scale a double weight to unsigned short "fixed point" one
	 */
	int scaleWeight(double weight) const
	{
		return (int) (weight*((double)numeric_limits<unsigned short>::max()));
	}

	/**
	 * scale a unsigned short weight to double weight
	 */
	double scaleWeight(int weight) const
	{
		return ((double)weight/(numeric_limits<unsigned short>::max()));
	}

	/**
	 * @return true iff this is a learning game
	 */
	bool isLearning() const
	{
		return _learning;
	}

	/**
	 * set learning status for Game
	 * @param learn learning status
	 */
	void setLearning(bool learn)
	{
		_learning = learn;
	}

	/**
	 * set the last black Player played Move
	 * @param move last black Player Move
	 * @param val value of board with this Move
	 **/
	void setLastBlackMove(Move* move, int val)
	{
		if (_lastBlackMove) delete _lastBlackMove;
		_lastBlackMove = move;
		_lastBlackMoveValue = val;
	}

	/**
	 * set the last white Player played Move
	 * @param move last white Player Move
	 * @param val value of board with this Move
	 **/
	void setLastWhiteMove(Move* move, int val)
	{
		if (_lastWhiteMove) delete _lastWhiteMove;
		_lastWhiteMove = move;
		_lastWhiteMoveValue = val;
	}


	/**
	 * @return last Move of black Player 
	 **/
	Move* getLastBlackMove()
	{
		return _lastBlackMove;
	}

	/**
	 * @return last Move of white Player 
	 **/
	Move* getLastWhiteMove()
	{
		return _lastWhiteMove;
	}

	/**
	 * @return value of board using last Move of black Player
	 **/
	int getLastBlackMoveValue() const
	{
		return _lastBlackMoveValue;
	}

	/**
	 * @return value of board using last Move of white Player
	 **/
	int getLastWhiteMoveValue() const
	{
		return _lastWhiteMoveValue;
	}

	/**
	 * @return previous Board played by computer Player
	 **/
	Board* getPreviousBoard() 
	{
		unsigned index = (_lastBlackMove && _lastWhiteMove)? 2 : 1;
		if (_boardsHistory.size() > index)
		{
			list<Board*>::iterator it = _boardsHistory.begin();
			if (index == 2) ++it;
			return *(++it);
		}
		return 0;
	}

	/**
	 * @return true iff "online learning" is permitted
	 */
	bool onlineLearningPermitted() const 
	{
		return _allowOnlineLearning;
	}

	static void setOnlineLearningActive(bool b)
	{
		_onlineLearningActive = b;
	}

	static bool isOnlineLearningActive()
	{
		return _onlineLearningActive;
	}


	/**
	* destruct Game, free memory
	**/
	~Game();
};


#endif /* __GAME_H */