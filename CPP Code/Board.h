/*****************************************************************************
 *               Dam Ka ! An Intelligent Game Of Checkers                    *
 *                  Workshop In Reinforcement Learning                       *
 *                      Ron Cohen          Yaniv Fais                        *
 *****************************************************************************/

#ifndef __BOARD_H
#define __BOARD_H

#include <math.h>
#include <list>
#include <vector>
#include <iostream>
#include <bitset>
#include <limits>


#include "Move.h"
#include "Player.h"
#include "Checkers.h"


#define MaxRow size
#define MaxCol size/2
#define BoardLength N

#include "Book.h"

/**
 * Board in the Game,parent for template
 */
class Board
{
public:
	~Board()
	{
	}

};




/**
 * Board with Parameters
 * where N is number of bits in board
 */
template <size_t N>
class ParamBoard : public Board
{
private:

	/**
	 * board is size * size
	 */
	static char size;

public:

	/**
	 * construct a parameters board with units
	 * @param whitePeonsP white peons units
	 * @param blackPeonsP black peons units
	 * @param whiteKingsP white Kings units
	 * @param blackKingsP black Kings units
	 */
	ParamBoard<N>(bitset<N> whitePeonsP=0,
				  bitset<N> blackPeonsP=0,
				  bitset<N> whiteKingsP=0,
				  bitset<N> blackKingsP=0)
			: whitePeons(whitePeonsP),blackPeons(blackPeonsP),
			 whiteKings(whiteKingsP),blackKings(blackKingsP)

	{
	}

	/**
	 * copy constuct a parameters board
	 */
	ParamBoard<N>(const ParamBoard<N>& b)
		: whitePeons(b.whitePeons),
		  blackPeons(b.blackPeons),
		  whiteKings(b.whiteKings),
		  blackKings(b.blackKings)
	{
	}


	/**
	 * destruct boars
	 */
	~ParamBoard<N>()
	{
	}

	/**
	 * make a move from this board
	 * @param move The move to perform
	 * @param color performing Player's color
	 * @return ParamBoard after move
	 */
	ParamBoard* makeMove(Move* move, int color) const
	{
		ParamBoard* newBoard = new ParamBoard(whitePeons,blackPeons,whiteKings,blackKings);

		bitset<N> *myPeons, *myKings, *opponentPeons, *opponentKings;
		if (color == COLOR_BLACK)
		{
			myPeons = &(newBoard->blackPeons);
			myKings = &(newBoard->blackKings);
			opponentPeons = &(newBoard->whitePeons);
			opponentKings = &(newBoard->whiteKings);
		}
		else
		{
			myPeons = &(newBoard->whitePeons);
			myKings = &(newBoard->whiteKings);
			opponentPeons = &(newBoard->blackPeons);
			opponentKings = &(newBoard->blackKings);
		}
		if ((*myPeons)[move->hops.front()]) // The moved piece is a peon
		{
			myPeons->reset(move->hops.front());
			if (color == COLOR_WHITE &&
			   (move->hops.back() >= (((int)N)-(newBoard->size/2)))) // last line -> set king
				myKings->set(move->hops.back());
			else if (color == COLOR_BLACK && move->hops.back() < newBoard->size/2) // last line -> set king
				myKings->set(move->hops.back());
			else
				myPeons->set(move->hops.back());
		}
		else // The moved piece is a king
		{
			myKings->reset(move->hops.front());
			myKings->set(move->hops.back());
		}
		list<int>::iterator it = move->hits.begin();
		for (; it != move->hits.end(); it++)
		{
			opponentPeons->reset(*it);
			opponentKings->reset(*it);
		}
		return newBoard;
	}

	/**
	 * find legel moves from this board for specified unit
	 * @param opponent all of opponent's units
	 * @param row row of starting point
	 * @param col coloumn of starting point
	 * @param king true iff starting point is a king
	 * @param hit to be set iff hitting
	 * @param color color of moving player
	 * @return all legel moves for unit on this board
	 **/
	Moves * findLegalMoves(bitset<N> opponent, char row, char col,
							bool king, bool *hit, int color) const
	{
		Moves *moves = new Moves;
		char r,c;
		char rowAdd = (color == COLOR_BLACK)? -1:1;
		if (!king)
			r = row+rowAdd;
		else
			r = row - 2;
		// go over possilbe moves, 2*2 squares from me..
		while ((king && r<=row+2) || (!king && r!=row+rowAdd*3))
		{
			char rowDistance = abs(r - row);
			if (!*hit || rowDistance==2)
			{
				for (c = col - rowDistance;c <= col+rowDistance;c+=2)
				{
					if ((r+c)%2 == 0 || c==col) continue;
					if (r>=0 && r<size && c>=0 && c<size)
					{
						char j = r * (size)/2 + c / 2;
						if (blackPeons[j] || whitePeons[j] || blackKings[j] || whiteKings[j])
							continue; // can't land on a peon/king

						if (rowDistance == 2)
						{
							char skippedCol = (c+col)/2;
							char skippedRow = (r+row)/2;

							char skipped = skippedRow * (size)/2 + skippedCol / 2;
							if (opponent[skipped]) // hit
							{
								if (!*hit)
								{
									Moves::iterator it = moves->begin();
									for (;it != moves->end(); it++)
									{
										Move * move = *it;
										delete move;
									}
									moves->clear();
								}
								*hit = true;
								bitset<N> op = opponent;
								op.reset(skipped);
								Moves * mvs = findLegalMoves(op, r, c, king, hit, color);

								char sz = mvs->size();

								if (sz == 0) // must continue capturing
								{			 // only if size == 0 add this Move
									Move* move = new Move;
									move->hops.push_back(j);
									move->hits.push_back(skipped);
									moves->push_back(move);
								}
								Moves::iterator it = mvs->begin();
								for (; it != mvs->end(); it++)
								{
									Move* move = *it;
									move->hops.push_front(j);
									move->hits.push_front(skipped);
									moves->push_back(move);
								}
								delete mvs;
							}
						}
						else
						{
							Move* move = new Move;
							move->hops.push_back(j);
							moves->push_back(move);
						}
					}
				}
			}
			if (!king)
				r += rowAdd;
			else
				r++;
		}
		return moves;
	}

	/**
	 * find all legal moves for player with color on this board
	 * @param color color of moving player
	 * @return all moves for player on this board
	 */
	Moves * findLegalMoves(int color) const
	{
		Moves *moves = new Moves;
		bitset<N> peons = (color == COLOR_BLACK)? blackPeons : whitePeons;
		bitset<N> kings = (color == COLOR_BLACK)? blackKings : whiteKings;
		bitset<N> opponent = (color == COLOR_BLACK)? (whitePeons | whiteKings) : (blackPeons | blackKings);
		char i = 0;
		char row, col;
		bool peon;
		bool king;
		bool hit = false;
		for (row = 0; row<size; row++)
		{ // go over all rows & cols & my units..
			for (col = 0; col<size; col++)
			{
				if ((row+col)%2 == 0) continue;
				king = false;
				peon = false;
				if (peons[i]) // there is a peon in square i
					peon = true;
				else if (kings[i]) // there is a king in square i
					king = true;
				if (peon || king)
				{
					bool mustHit = hit;
					Moves * mvs = this->findLegalMoves(opponent, row, col, king, &mustHit, color);
					if (mustHit && !hit)
					{
						Moves::iterator it = moves->begin();
						for (;it != moves->end(); it++)
						{
							Move * move = *it;
							delete move;
						}
						moves->clear();
						hit = true;
					}
					Moves::iterator it = mvs->begin();
					for (; it != mvs->end(); it++)
					{
						Move* move = *it;
						move->hops.push_front(i);
						moves->push_back(move);
					}
					delete mvs;
				}
				i++;
			}
		}
		return moves;
	}

	/**
	 * @return number of pieces on board
	 */
	int piecesCount() const
	{
		return (blackPeons | blackKings | whitePeons | whiteKings).count();
	}

	/**
	 * @return stage in game
	 */
	int getStage() const
	{
		bitset<N> bits = blackPeons;
		bits |= blackKings;
		bits |= whitePeons;
		bits |= whiteKings;
		int count = bits.count();
		switch (N)
		{
		case 32:
			return (int)ceil(count/4.0) - 1;
		case 18:
			return (int)ceil(count/2.0) - 1;
		case 50:
			return (int)ceil(count/7.0) - 1;
		}
		return -1;
	}

	/**
	 * Returns the bits representation of the board
	 * @return Board bits representation, as string
	 */
	string getBoardValue()
	{
		bitset<4*N> bits = 0;
		string str = "";
		int i;
		for (i=0; i<N; i++)
		{
			bits.set(i, whiteKings.at(i));
			bits.set(N+i, blackKings.at(i));
			bits.set(2*N+i, whitePeons.at(i));
			bits.set(3*N+i, blackPeons.at(i));
		}
		for (i=0; i<N/2; i++)
		{
			char x = 0;
			for (int j=0; j<8; j++)
			{
				x |= (char)bits.at(4*N-(i*8+j)-1);
				if (j < 7) x <<= 1;
			}
			str += x;
		}
		return str;
	}



	/**
	 * evaluate a board
	 * @param color evaluating player's color
	 * @param weights parameters weights (coefficiants)
	 * @param values to be set with values of parametes
	 * @return evaluation value of ParamBoard
	 */
	 int eval(char color, unsigned short* weights, int *values)
	 {
		updateFields(color); // update date used by parameters
		int oppColor = (color == COLOR_BLACK)? COLOR_WHITE : COLOR_BLACK;
		bool learn = Game::getInstance()->isLearning();

		short opponentHitting = 0;
		values[0] = learn | isActiveWeight(weights[0]) ? (piecesAdvantage() * weights[0]) : 0;
		values[1] = learn | isActiveWeight(weights[1]) ? (opponentLiberty(oppColor,opponentHitting) * weights[1]) : 0;
		values[2] = learn | isActiveWeight(weights[2]) ? (kings() * weights[2]) : 0;
		values[3] = learn | isActiveWeight(weights[3]) ? (centerControl() * weights[3]) : 0;
		values[4] = learn | isActiveWeight(weights[4]) ? (kingsCenterControl() * weights[4]) : 0;
		values[5] = learn | isActiveWeight(weights[5]) ? (opponentCenterControl() * weights[5]) : 0;
		values[6] = learn | isActiveWeight(weights[6]) ? (advancement(color) * weights[6]) : 0;
		values[7] = learn | isActiveWeight(weights[7]) ? (oppKings() * weights[7]) : 0;
		values[8] = learn | isActiveWeight(weights[8]) ? (OpponentGuard(color) * weights[8]) : 0;
		values[9] = learn | isActiveWeight(weights[9]) ?  (cramp(color) * weights[9]) : 0;
		values[10] = learn | isActiveWeight(weights[10]) ? (doubleDiagonalFile() * weights[10]) : 0;
		values[11] = learn | isActiveWeight(weights[11]) ? (diagonalMomentValue() * weights[11]) : 0;
		values[12] = learn | isActiveWeight(weights[12]) ? (dyke(color) * weights[12]) : 0;
		values[13] = learn | isActiveWeight(weights[13]) ? (exposure() * weights[13]) : 0;
		values[14] = learn | isActiveWeight(weights[14]) ? (gap() * weights[14]) : 0;
		values[15] = learn | isActiveWeight(weights[15]) ? (hole() * weights[15]) : 0;
		values[16] = learn | isActiveWeight(weights[16]) ? (node() * weights[16]) : 0;
		values[17] = learn | isActiveWeight(weights[17]) ? (pole() * weights[17]) : 0;
		values[18] = learn | isActiveWeight(weights[18]) ? (backRowControl(color) * weights[18]) : 0;
		values[19] = opponentHitting * weights[19];
		int value = 0;
		for (register char i=0; i<PARAMS_NUM; i++)
			value += values[i];
		return value;
	}

	/**
	 * test equality with another Board
	 * @param lhs other board
	 * @return true iff this == other
	 */
	bool equals(const ParamBoard<N>& lhs) const
	{
		return (whitePeons == lhs.whitePeons && whiteKings == lhs.whiteKings
				&& blackPeons == lhs.blackPeons && blackKings == lhs.blackKings);
	}

	/**
	 * @return white player's peons
	 */
	const bitset<N>& getWhitePeons() const
	{
		return whitePeons;
	}

	/**
	 * @return black player's peons
	 */
	const bitset<N>& getBlackPeons() const
	{
		return blackPeons;
	}

	/**
	 * @return white player's kings
	 */
	const bitset<N>& getWhiteKings() const
	{
		return whiteKings;
	}

	/**
	 * black player's kings
	 */
	const bitset<N>& getBlackKings() const
	{
		return blackKings;
	}


private:
	/**
	 * white player's peons
	 */
	bitset<N> whitePeons;

	/**
	 * black player's peons
	 */
	bitset<N> blackPeons;

	/**
	 * white player's kings
	 */
	bitset<N> whiteKings;

	/**
	 * black player's kings
	 */
	bitset<N> blackKings;

   /**
	* active men in turn
	*/
	bitset<N> * activeMen;

   /**
	* passive men in turn
	*/
	bitset<N> * passiveMen;

   /**
	* active kings in turn
	*/
	bitset<N>* activeKings;

   /**
	* passive kings in turn
	*/
	bitset<N> * passiveKings;

   /**
	* total actives in turn
	*/
	bitset<N> actives;

   /**
	* total passives in turn
	*/
	bitset<N> passives;

   /**
	* total pieces in tuen
	*/
	bitset<N> pieces;


	// special squares used by parameters
	static char centerField[8];// = {7,8,11,12,16,17,20,21};
	static char doubleCorners[2];// = {3,28};
	static char otherNearCrampWhite[4],otherNearCrampBlack[4],nearCrampWhite[2],nearCrampBlack[2];
	static char triangleOfOreoWhite[3],triangleOfOreoBlack[3]; //{25,29,30} : {1,2,6};
	static char backRowBridgeWhiteSquares[2],backRowBridgeBlackSquares[2];
	static char whiteCrampingSquare,blackCrampingSquare;

   /**
	* @param weight A weight of parameter
	* @return if weight is active (not below threshold)
	*/
	inline bool isActiveWeight(unsigned short weight) const
	{
		return (weight > WEIGHT_THRESHOLD);
	}

	/**
	 * update the board fields used to calculate parameters
	 * @param color evaluating Player's color
	 */
	void updateFields(char color)
	{
		if (color == COLOR_BLACK)
		{
			passiveMen = &whitePeons;
			activeMen = &blackPeons;
			passiveKings = &whiteKings;
			activeKings = &blackKings;
			actives = blackPeons | blackKings;
			passives = whitePeons | whiteKings;
		}
		else
		{
			passiveMen = &blackPeons;
			activeMen = &whitePeons;
			passiveKings = &blackKings;
			activeKings = &whiteKings;
			passives = blackPeons | blackKings;
			actives = whitePeons | whiteKings;
		}
		pieces = actives | passives;
	}

// all parameters are scaled to return values ranging in the same area

	/**
	 * Pieces Advantage.
	 * This parameter counts the difference in number of pieces between the two players.
	 * where king counts as 3 points and peon 2.
	 * Since the target of the game is to hit all opponent pieces having this advantage
	 * is considered a good attack parameter.
 	 * @return parameter value
	 */
	short piecesAdvantage() const
	{
		return ((3*(activeKings->count() - passiveKings->count()) +
				2*(activeMen->count() - passiveMen->count()))<<2);
	}

	/**
	 * This parameter counts the number of available moves for the opponent
	 * (as negative number) ;
	 * disregarding the fact that hit moves are compulsory.
	 * Once the opponent has no available moves it loses,
	 * therefore having the highest value (close to zero)
	 * meaning opponent is near lose.
	 * @param oppColor color of opponent of evaluator
	 * @param opponentHitting set with opponentHit parameter which is -1 if opponent must hit
 	 * @return parameter value
	 */
	short opponentLiberty(char oppColor,short& opponentHitting) const
	{
		Moves* moves = findLegalMoves(oppColor);
		if (moves)
		{
			short count = moves->size();
			if (count)
				opponentHitting =  -((moves->front()->hits.size()>0)<<5);
			for (Moves::iterator it = moves->begin(); it != moves->end(); ++it)
			{
				delete *it;
			}
			delete moves;
			return -(count);

		}
		return 0;
	}

	/**
	 * Kings.
	 * @return the number of kings of the evaluating player.
	 */
	short kings() const
	{
		return (activeKings->count()<<2);
	}

	/**
	 * Opponent Kings.
	 * @return the number of kings of the opponent.
	 */
	short oppKings() const
	{
		return -(passiveKings->count()<<2);
	}

	/**
	 * Center Control
	 * This parameter counts the number of peons the evaluating player has on
	 * the center squares: Having pieces on these squares gives a strong
	 * advantage in control of the game since opponent is blocked for moving
	 * it’s pieces ahead.
 	 * @return parameter value
	 */
	short centerControl() const
	{
		short credits = 0;
		for (char i = 0 ; i < sizeof(centerField) ; i++)
				credits += activeMen->test(centerField[i]);
		return (credits<<2);
	}

	/**
	 * Kings Center Control
	 * @return the number of kings the player has on the center squares
	 */
	short kingsCenterControl() const
	{
		short credits = 0;
		for (char i = 0 ; i < sizeof(centerField) ; i++)
				credits += activeKings->test(centerField[i]);
		return (credits<<2);
	}

	/**
	 * This parameter counts the number of center
	 * squares occupied by an opponent piece (either peon or king),
	 * this parameter has negative value since having opponent holding
	 * center squares control is negative.
 	 * @return parameter value
	 */
	short opponentCenterControl() const
	{
		short credits = 0;
		for (char i = 0 ; i < sizeof(centerField) ; i++)
				credits += passives.test(centerField[i]);
		return -(credits<<2);
	}

	/**
	 * ADV (Advancement)
	 * The parameter is credited with 1 for each passive man in the 5th and 6th rows
	 * (counting in passive's direction) and debited with 1 for each passive man in
	 * the 3rd and 4th rows.
	 * @param color color of evaluator
	 * @return parameter value
	 */
	short advancement(char color) const// a.k.a ADV
	{ // assumming 3,4 are opponent's 5,6 rows....
		bitset <N> row3_4,row5_6;
		for (char i = 0; i < MaxCol*2 ; i++)
		{
			row3_4.set(MaxCol*2+i);
			row5_6.set(4*MaxCol+i);
		}
		row3_4 &= *passiveMen;
		row5_6 &= *passiveMen;

		return (color == COLOR_BLACK)
			? ((row5_6.count() - row3_4.count())<<2)
			: ((row3_4.count() - row5_6.count())<<2);
	}



	/**
	 * Back Row Bridge (Modified)
	 * Credited with -1 if the evaluating player has no kings, and the two bridge squares
	 * in the back row are occupied by the opponent player.
	 * These squares play an important row in control of defense of the last row pieces.
	 * @param color evaluating Player's color
	 * @return parameter value
	 */
	short OpponentGuard(char color) const// a.k.a BACK
	{
		char * backRowBridgeSquares =
			(color==COLOR_WHITE) ? &(backRowBridgeWhiteSquares[0]) : &(backRowBridgeBlackSquares[0]);
		for (char i = 0 ; i < sizeof(backRowBridgeWhiteSquares) ; i++)
			if (!passives.test(backRowBridgeSquares[i]))
				return 0;
		return -((activeKings->count()==0)<<5);
	}


	/**
	 * This parameter is credited with -1 if the opponent player occupies the
	 * cramping square (19 for black, 12 for white) and at least one of
	 * the nearby squares (18 or 23 for black, 8 or 13 for white),
	 * while the evaluating player occupies all of the following squares:
	 * 7, 10, 11, 15 (white opponent) or 16, 20, 21, 24 (black opponent).
	 * These parameter is used to determine control of side diagonal squares
	 * which have an important row in defending the back rows pieces.
	 * @param color evaluator Player's color
	 * @return parameter value
	 */
	short cramp(char color) const// a.k.a CRAMP
	{
		char crampingSquare = (color == COLOR_BLACK) ? whiteCrampingSquare : blackCrampingSquare;
		if (passives[crampingSquare])
		{
			char * nearCrampingSquares  = (color == COLOR_BLACK) ? &(nearCrampWhite[0]) : &(nearCrampBlack[0]); // {18,23} : {8,13};
			char * otherSquares  = (color == COLOR_BLACK) ?  &(otherNearCrampWhite[0]) : &(otherNearCrampBlack[0]); // {16,20,21,24} : {7,10,11,15};
			if (passives[nearCrampingSquares[0]] || passives[nearCrampingSquares[1]])
			{
				if (   actives[otherSquares[0]] || actives[otherSquares[1]]
					|| actives[otherSquares[2]] || actives[otherSquares[3]])
						return (-32);
			}
		}
		return 0;
	}


	/**
	 * This parameter is credited with 1 for each piece of the
	 * opponent player that occupies one of the following squares:
	 * 3, 7, 10, 14, 17, 21, 24, 28. These square are on the longest
	 * diagonal with double corners which can be used to measure tool mobility on board.
	 * @return parameter value
	 */
	short doubleDiagonalFile() const// a.k.a DIA
	{
		char diagonal = 0,credits = 0;
		for (char i = 0;i < MaxRow; i++)
		{
			diagonal += (i%2 == 0) ? MaxCol-1 : MaxCol;
			credits += passives[diagonal];
		}
		return -(credits<<2);
	}

	/**
	 * This parameter is credited with -3 points for each piece of the opponent
	 * player that occupies one of the double diagonal squares,
	 * -2 points for each piece on squares: 2,6,9,13,16,20,11,15,18,22,25,29,
	 * and -1 point for pieces on squares: 1,5,8,12,19,23,26,30.
	 * The parameter tests the distance from the longest double corner diagonal
	 * and credits player which puts it’s pieces closer to that line.
	 * @return parameter value
	 **/
	short diagonalMomentValue() const//a.k.a DIAV
	{ // changed to two times from Samuel's definition to keep it int
		char diagonal = 0,credits = 0;
		for (char i = 0;i < MaxRow; i++)
		{
			diagonal += (i%2 == 0) ? MaxCol-1 : MaxCol;
			char diagonalR2m = (((diagonal - 2) >= MaxCol*i) ? passives[diagonal-2] : 0);
			char diagonalR2p = (((diagonal + 2) < MaxCol*(i+1)) ? passives[diagonal+2] : 0);
			char diagonalR1m = (((diagonal - 1) >= MaxCol*i) ? passives[diagonal-1]*2 : 0);
			char diagonalR1p = (((diagonal + 1) < MaxCol *(i+1)) ? passives[diagonal+1]*2 : 0);
			credits += (diagonalR2m+diagonalR2p) + (diagonalR1m + diagonalR1m) + 3*passives[diagonal];
		}
		return -(credits);
	}

	/**
	 * This parameter is credited with -1 for each string of the
	 * opponent player's pieces that occupy three adjacent diagonal squares.
	 * Having three adjacent diagonal squares in control is considered a good thing
	 * since non of them can be hit along this row since they are protecting one another.
	 * @param color evaluator Player's color
	 * @return parameter value
	 */
	short dyke(char color) const// a.k.a DYKE
	{
		char  diagonals[3];
		char row ,col,credits = 0;
		for (char i = 0; i < MaxRow-1; i++) // left to right
		{
			if (i<MaxCol)
			{
				diagonals[0] = i;
				row = 0;
				col = i*2+1;
			}
			else {
				diagonals[0] = MaxCol*((i-MaxCol)*2+1);
				row = ((i-MaxCol)*2+1);
				col = 0;
			}

			for (; (row < MaxRow - 2) && (col < MaxRow - 2); row++,col++,diagonals[0]=diagonals[1])
			{
			  diagonals[1] = diagonals[0]+((row%2 == 0) ? MaxCol+1 : MaxCol);
			  diagonals[2] = diagonals[0]+MaxCol*2+1;
			  if (passives[diagonals[0]] && passives[diagonals[1]] && passives[diagonals[2]])
				  credits++;
			}
		}
		for (i = 1; i < MaxRow - 2; i++) // right to left
		{
			if (i<MaxCol)
			{
				diagonals[0] = i;
				row = 0;
				col = i*2+1;
			}
			else
			{
				diagonals[0] = MaxRow*(i-MaxCol+1)+MaxCol-1;
				row = ((i-MaxCol+1)*2);
				col = MaxCol*2-1;
			}
			for (; (row < MaxRow - 2) && (col >=  2); row++,col--,diagonals[0]=diagonals[1])
			{
			  diagonals[1] = diagonals[0]+((row%2 == 0) ? MaxCol : MaxCol-1);
			  diagonals[2] = diagonals[0]+MaxCol*2-1;
			  if (passives[diagonals[0]] && passives[diagonals[1]] && passives[diagonals[2]])
				  credits++;
			}
		}
		return -(credits<<2);
	}

	/**
	 * This parameter is credited with 1 for each of the opponent player's
	 * pieces that are flanked along one or the other diagonal by two empty squares.
	 * Having empty squares on both ends is considered a real flow for a piece since it
	 * can't defend it self.
	 * @return parameter value
	 **/
	short exposure() const// a.k.a EXPOS
	{
		char i = 0;
		char credits = 0;
		for (char iRow = 1; iRow < MaxRow-1; iRow++)
		{
			for (char iCol = 0; iCol < MaxCol ; iCol++,i++)
			{
				if ((iRow %2 == 1 && iCol == 1)||(iRow%2 == 0 && iCol == MaxCol-1))
					continue;
				int col1a,col1b,col2a,col2b;
				if (passives.test(i))
				{
				  if (iRow % 2 == 0)
				  {
					col1b = MaxCol - 1;
					col2a =  MaxCol + 1;
					col2b = col1a  = MaxCol;

				  }
				  else {
					col1a =  MaxCol + 1;
					col1b = col2a = MaxCol;
					col2b = MaxCol - 1;
				  }
				  bool credit = false;
				  if (!pieces.test(i + col1a ) && !pieces.test(i + col2a))
					  credit = true;
				  if (!pieces.test(i + col1b) && !pieces.test(i + col2b ))
					  credit = true;
				  credits+=credit;
				}
			}
		}
		return (credits<<2);
	}

	/**
	 * This parameter is credited with 1 for each single empty square
	 * that separates two of the opponent player's pieces along a diagonal,
	 * or separates one of it's pieces from the edge of the board.
	 * Having such a gap square is a parameter for attacking ability since such
	 * square enables player to hit opponent.
	 * @return parameter value
	 **/
	short gap() const// a.k.a GAP
	{
		char credits = 0;
		for (char i = 0 ; i < BoardLength ; i++ )
		{
			if (!pieces.test(i))
			{
				bool credit = false;
				char row = i/MaxCol;
				char oddRow = row % 2;
				char i1 = i + MaxCol +1- oddRow;
				char i2 = i - MaxCol - oddRow;
				if (i1 < BoardLength && i2 > 0)
				{
				  if (passives.test(i1) && passives.test(i2))
					credit = true;
				}
				else credit = true;
				i1 = i + MaxCol - oddRow;
				i2 = i - MaxCol +1-oddRow;
				if (i1 < BoardLength && i2 > 0)
				{
				  if (passives.test(i1) && passives.test(i2))
					credit = true;
				}
				else credit = true;
				credits+=credit;
			}
		}
		return credits;
	}


	/**
	 * This parameter is credited with 1 for each empty square that is surrounded by
	 * three or more of the opponent player's pieces. Having such a hole is suggests
	 * a strong hold of some region.
	 * @return parameter value
	 **/
	short hole() const// a.k.a HOLE
	{
		char credits = 0;
		for (char row = 1; row < MaxRow -1 ; row++)
		{
			char oddRow = row % 2;
			for (char col = 1; col < MaxCol-1+oddRow; col++)
			{
				char i = row*MaxCol+col;
				if (!pieces.test(i))
				{
					char surround = 0;
					surround += passives.test(i+MaxCol);
					surround += passives.test(i+MaxCol+1-2*oddRow);
					surround += passives.test(i-MaxCol);
					surround += passives.test(i-MaxCol+1-2*oddRow);
					credits += (surround>=3);
				}
			}
		}
		return -(credits<<3);
	}


	/**
	 * This parameter is credited with 1 for each of the opponent
	 * player's pieces that are surrounded by at least 3 empty squares.
	 * @return parameter value
	 **/
	short node() const// a.k.a NODE
	{
		char credits = 0;
		for (char row = 1; row < MaxRow -1 ; row++)
		{
			int oddRow = row%2;
			for (char col = 1; col < MaxCol-1+(oddRow); col++)
			{
				char i = row*MaxCol+col;
				if (passives.test(i))
				{
					char surround = 0;
					surround += (!pieces.test(i+MaxCol));
					surround += (!pieces.test(i+MaxCol+1-2*oddRow));
					surround += (!pieces.test(i-MaxCol));
					surround += (!pieces.test(i-MaxCol+1-2*oddRow));
					credits += (surround>=3);
				}
			}
		}
		return (credits<<2);
	}

	/**
	 * This parameter is credited with 1 for each of the opponent player's
	 * pieces that are completely surrounded by empty squares.
	 * Having such a lone piece is not good since it can't be helped
	 * by other player's pieces either for defense or for later second strike
	 * move in case of hit.
	 * @return parameter value
	 **/
	short pole() const // a.k.a POLE
	{
		char credits = 0;
		for (char row = 1; row < MaxRow -1 ; row++)
		{
			char oddRow = row%2;
			for (char col = 1; col < MaxCol-1+oddRow; col++)
			{
				char i = row*MaxCol+col;
				if (passiveMen->test(i))
				{
					char surround = 0;
					surround += (!pieces.test(i+MaxCol));
					surround += (!pieces.test(i+MaxCol+1-2*oddRow));
					surround += (!pieces.test(i-MaxCol));
					surround += (!pieces.test(i-MaxCol+1-2*oddRow));
					credits += (surround==4);
				}
			}
		}
		return (credits<<2);
	}

	/**
	 * This parameter is credited with 1 if the evaluating player pieces occupy
	 * both the back row bridge and the "Triangle of Oreo",
	 * defined as squares 25,29,30 for black, or 1,2,6 for white.
	 * This parameter is used for determining the control of the back row
	 * which opponent needs to reach to for crowning a king.
	 * @param color evaluator Player's color
	 * @return parameter value
	 **/
	short backRowControl(char color) const// a.k.a GUARD
	{
		char * triangleOfOreoSquares =
		  (color == COLOR_WHITE) ? &(triangleOfOreoWhite[0]) : &(triangleOfOreoBlack[0]); //{25,29,30} : {1,2,6};
		for (char i=0; i < sizeof(triangleOfOreoSquares) ; i++)
			if (!actives.test(*(triangleOfOreoSquares+i)))
				return 0;
		return ((passiveKings->count()==0)<<5);
	}

};


#endif
