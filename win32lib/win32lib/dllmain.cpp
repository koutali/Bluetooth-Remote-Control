/* Replace "dll.h" with the name of your header */
#include <windows.h>
#include "mouse_MouseCursor.h"

JNIEXPORT jdoubleArray JNICALL Java_mouse_MouseCursor_GetCursorPos
  (JNIEnv *j_env, jobject j_obj){
   
    jdoubleArray ret = j_env->NewDoubleArray(2);
    POINT ptCursorPos;
	GetCursorPos(&ptCursorPos);
	double x[2];
	x[0] = (double)ptCursorPos.x;
	x[1] = (double)ptCursorPos.y;
    j_env->SetDoubleArrayRegion(ret,0,2,(jdouble*)x);
    return ret;          

}
BOOL APIENTRY DllMain (HINSTANCE hInst     /* Library instance handle. */ ,
                       DWORD reason        /* Reason this function is being called. */ ,
                       LPVOID reserved     /* Not used. */ )
{
    switch (reason)
    {
      case DLL_PROCESS_ATTACH:
        break;

      case DLL_PROCESS_DETACH:
        break;

      case DLL_THREAD_ATTACH:
        break;

      case DLL_THREAD_DETACH:
        break;
    }

    /* Returns TRUE on success, FALSE on failure */
    return TRUE;
}
