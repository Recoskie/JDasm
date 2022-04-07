package Format.MACDecode;

public class Pointers { long loc = 0, size = 0; int ptr_size = 0, node = 0; public Pointers( long Loc, long Size, int Ptr_size, int Node ){ loc = Loc; size = Loc + Size; ptr_size = Ptr_size; node = Node; } }
