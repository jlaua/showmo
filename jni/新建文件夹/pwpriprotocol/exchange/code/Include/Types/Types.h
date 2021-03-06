

#ifndef __XC_TYPES_H__
#define __XC_TYPES_H__

#include "../Infra/Types.h"

//////////////////////////////////////////////////////////////////////////
// base types definition
// compilers should be restricted to ensure the following equalities!

#ifndef __SMALL_FILE_TIME_DEFINED__
#define __SMALL_FILE_TIME_DEFINED__
//时间结构
typedef struct _DATETIME								
{
#ifdef __BIG_ENDIAN__
	uint year		:6;				//	年	2000-2063	
	uint month		:4;				//	月	1-12		
	uint day		:5;				//	日	1-31		
	uint hour		:5;				//	时	0-23		
	uint minute		:6;				//	分	0-59		
	uint second		:6;				//	秒	0-59	
#else
	uint second		:6;				//	秒	0-59		
	uint minute		:6;				//	分	0-59		
	uint hour		:5;				//	时	0-23		
	uint day		:5;				//	日	1-31		
	uint month		:4;				//	月	1-12		
	uint year		:6;				//	年	2000-2063	
#endif
}DateTime,*pDATETIME;

#endif

typedef union {	//IP addr
	uchar	c[4];
	ushort	s[2];
	uint	l;
} IPAddress;
#include "../GDI/Types.h"
//#ifndef _WIN32_STIMULATE
	typedef struct {
		int left;
		int top;
		int right;
		int bottom;
	}RECT;
//#endif

#endif
