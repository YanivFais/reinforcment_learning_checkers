/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/
 #ifndef __BOOK_H
#define __BOOK_H

#include <bitset>
#include <fstream>
#include <string>
#include "Board.h"
#include "GameBoard.h"
#include "Checkers.h"

using namespace std;

/**
 * Book for good Moves in the Game
 **/
class Book
{
protected:

	char _bookLevel;

public:
	/**
	 * get a move for Player with color for Board from Book
	 * @param board Board to Move from
	 * @param color color of Player performing Move
	 * @return Move played from Book
	 */
	virtual Move* getBookMove(Board* board, int color)
	{
		return 0;
	}

	/**
	 * add a move for Player with color for Board from Book
	 * @param board Board to Move from
	 * @param color color of Player performing Move
	 * @param move Move to add to Book
	 */
	virtual void addBookMove(Board* board, int color, Move* move)
	{
	}	

	virtual ~Book()
	{
	}


	char getBookLevel()
	{
		return _bookLevel;
	}
};


/**
 * concrete Book for Moves as template by Board size
 **/
template<size_t N>
class ConcreteBook : public Book
{
public:

	/**
	 * construct a Book from default file
	 */
	ConcreteBook()
	{
		char fname[30];
		sprintf(fname, "%s%s%s%d%s",DATA_DIRECTORY,DIR_DELIMITER,BOOK_FILE_PREFIX_NAME, N,BOOK_FILE_EXTENSION);
		fs.open(fname, ios::in | ios::out | ios::binary);
		if (!fs.is_open()) 
		{
			_bookLevel = 0;
		}
		else 
		{
			fs.read(&_bookLevel, 1);
		}
	}
	/**
	 * get a move for Player with color for Board from Book
	 * @param b Board to Move from
	 * @param color color of Player performing Move
	 * @return Move played from Book
	 */
	Move* getBookMove(Board* b, int color)
	{
		ParamBoard<N>* board = (ParamBoard<N>*)b;

		bitset<N*4> bits;

		// decode board for file
		const bitset<N>& kings = board->getWhiteKings();
		const bitset<N>& peons = board->getWhitePeons();
		const bitset<N>& oppKings = board->getBlackKings();
		const bitset<N>& oppPeons = board->getBlackPeons();

		if (color == COLOR_BLACK)
		{
			for (int i=0; i<N; i++)
			{
				bits.set(i, oppPeons.at(i));
				bits.set(N+i, oppKings.at(i));
				bits.set(2*N+i, peons.at(i));
				bits.set(3*N+i, kings.at(i));
			}
		}
		else
		{
			for (int i=0; i<N; i++)
			{
				bits.set(i, oppPeons.at(N-i-1));
				bits.set(N+i, oppKings.at(N-i-1));
				bits.set(2*N+i, peons.at(N-i-1));
				bits.set(3*N+i, kings.at(N-i-1));
			}
		}
		if (GameBoard::getStage(b) < STAGES-1) return 0;
		fs.seekg(1, ios_base::beg);

		bool match = false;

		// search in file
		while (!match && fs.good())
		{
			int c = 0;
			for (int i=0; i<N/2; i++)
			{
				char y;
				fs.read(&y, 1);
				char x = 0;
				for (int j=0; j<8; j++)
				{
					x |= (char)bits.at(4*N-(i*8+j)-1);
					if (j < 7) x <<= 1;
				}
				if (x==y) c++;
			}
			if (c == i)
				match = true;
			else
			{
				char t[8];
				fs.read(t, 8);
			}
		}

		if (match) // found record in file
		{
			{
				Move * mv = new Move;

				for (int i=0; i<8; i++) //decode record to Move hops
				{
					char hop;
					fs.read(&hop, 1);
					if (hop >= 0)
					{
						if (color == COLOR_BLACK)
						{
							hop = N - hop - 1;
						}
						mv->hops.push_back((int)hop);
					}
				}

				// find complete Move by Game (hits too)
				Moves* moves = GameBoard::findLegalMoves(b, color);
				bool found = false;
				for (Moves::iterator it = moves->begin(); it != moves->end(); ++it)
				{
					Move* move = *it;
					if (!found && move->hops.size() == mv->hops.size())
					{
						found = true;
						list<int>::iterator it1 = move->hops.begin();
						list<int>::iterator it2 = mv->hops.begin();
						for (;it1 != move->hops.end(); ++it1, ++it2)
						{
							if (*it1 != *it2) found = false;
						}
						if (!found) delete move;
						else
						{
							delete mv;
							mv = move;
						}
					}
					else delete move;
				}
				delete moves;
				return mv;
			}
		}
		else // haven't found
		{
			fs.clear();
			fs.seekp(0, ios::end);

		}
		return 0;
	}

	/**
	 * add a move for Player with color for Board from Book
	 * @param b Board to Move from
	 * @param color color of Player performing Move
	 * @param move Move to add to Book
	 */
	void addBookMove(Board* b, int color, Move* move)
	{
		ParamBoard<N>* board = (ParamBoard<N>*)b;

		bitset<N*4> bits;
		const bitset<N>& kings = board->getWhiteKings();
		const bitset<N>& peons = board->getWhitePeons();
		const bitset<N>& oppKings = board->getBlackKings();
		const bitset<N>& oppPeons = board->getBlackPeons();

		// encode for file stream record
		if (color == COLOR_BLACK)
		{
			for (int i=0; i<N; i++)
			{
				bits.set(i, oppPeons.at(i));
				bits.set(N+i, oppKings.at(i));
				bits.set(2*N+i, peons.at(i));
				bits.set(3*N+i, kings.at(i));
			}
		}
		else
		{
			for (int i=0; i<N; i++)
			{
				bits.set(i, oppPeons.at(N-i-1));
				bits.set(N+i, oppKings.at(N-i-1));
				bits.set(2*N+i, peons.at(N-i-1));
				bits.set(3*N+i, kings.at(N-i-1));
			}
		}
		if (GameBoard::getStage(board) < STAGES-1) // add only for Game start due to lack of resources
			return;


		// write to file stream an encoded record
		for (int i=0; i<N/2; i++)
		{
			char x = 0;
			for (int j=0; j<8; j++)
			{
				x |= (char)bits.at(4*N-(i*8+j)-1);
				if (j < 7) x <<= 1;
			}

			fs.write(&x, 1);
		}

		int hopsCount = 0;

		list<int>::iterator it = move->hops.begin();
		for (; it != move->hops.end(); it++)
		{
			hopsCount++;
			char square = *it;
			if (color == COLOR_BLACK)
			{
				square = N - square - 1;
			}
			fs.write(&square, 1);
		}
		for (i=hopsCount; i<8; i++)
		{
			char t=-1;
			fs.write(&t, 1);
		}
		fs.flush();
	}

	/**
	 * destructor,close file
	 */
	~ConcreteBook()
	{
		fs.close();
	}

private:

	/**
	 * Book file name
	 */
	string fname;

	/**
	 * Book file stream
	 */
	fstream fs;

};

#endif