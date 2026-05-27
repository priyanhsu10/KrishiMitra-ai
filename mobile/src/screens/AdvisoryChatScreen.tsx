import React, { useState, useRef, useCallback } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
} from 'react-native';
import { useSelector } from 'react-redux';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootState } from '../store';
import { AppTabParamList } from '../navigation/AppNavigator';
import { advisoryApi } from '../api/advisoryApi';

type AdvisoryScreenNavigationProp = StackNavigationProp<AppTabParamList, 'Advisory'>;

interface Props {
  navigation: AdvisoryScreenNavigationProp;
}

interface ChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  contentEn?: string;
  priority?: string;
  timestamp: string;
}

const STAGES = [
  { label: '🌱 Germination', value: 'germination' },
  { label: '🌿 Vegetative', value: 'vegetative' },
  { label: '🌸 Flowering', value: 'flowering' },
  { label: '🌾 Harvest', value: 'harvest' },
];

const CROPS = [
  { label: '🌱 Soybean', value: 'soybean' },
  { label: '🌿 Cotton', value: 'cotton' },
  { label: '🌾 Wheat', value: 'wheat' },
  { label: '🧅 Onion', value: 'onion' },
];

export default function AdvisoryChatScreen({ navigation }: Props) {
  const farmer = useSelector((state: RootState) => state.farmer);
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: 'welcome',
      role: 'assistant',
      content: `नमस्कार! मी तुमचा AI सल्लागार आहे. तुमच्या पिकाबद्दल काहीही विचारा.\n\nHello! I'm your AI farming assistant. Ask me anything about your crops.`,
      timestamp: new Date().toISOString(),
    },
  ]);
  const [inputText, setInputText] = useState('');
  const [loading, setLoading] = useState(false);
  const [selectedCrop, setSelectedCrop] = useState('soybean');
  const [selectedStage, setSelectedStage] = useState('vegetative');
  const flatListRef = useRef<FlatList>(null);

  const sendMessage = useCallback(async () => {
    const trimmed = inputText.trim();
    if (!trimmed || !farmer.id || !farmer.authToken) return;

    const userMsg: ChatMessage = {
      id: Date.now().toString(),
      role: 'user',
      content: trimmed,
      timestamp: new Date().toISOString(),
    };

    setMessages(prev => [...prev, userMsg]);
    setInputText('');
    setLoading(true);

    try {
      const response = await advisoryApi.getAdvisory(
        {
          farmer_id: farmer.id,
          crop_type: selectedCrop,
          stage: selectedStage,
          language: farmer.language || 'marathi',
          question: trimmed,
        },
        farmer.authToken
      );

      const assistantMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: response.advice || response.advice_mr || 'No advice available',
        contentEn: response.advice_en,
        priority: response.priority,
        timestamp: new Date().toISOString(),
      };

      setMessages(prev => [...prev, assistantMsg]);
    } catch (error: any) {
      const errorMsg: ChatMessage = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        content: 'क्षमस्व, सध्या सेवा अनुपलब्ध आहे. पुन्हा प्रयत्न करा.',
        timestamp: new Date().toISOString(),
      };
      setMessages(prev => [...prev, errorMsg]);
    } finally {
      setLoading(false);
      setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
    }
  }, [inputText, farmer, selectedCrop, selectedStage]);

  const renderMessage = ({ item }: { item: ChatMessage }) => (
    <View style={[styles.messageBubble, item.role === 'user' ? styles.userBubble : styles.assistantBubble]}>
      <Text style={[styles.messageText, item.role === 'user' && styles.userMessageText]}>
        {item.content}
      </Text>
      {item.contentEn && (
        <Text style={styles.messageTextEn}>{item.contentEn}</Text>
      )}
      {item.priority === 'high' && (
        <View style={styles.priorityBadge}>
          <Text style={styles.priorityText}>⚠️ High Priority Alert</Text>
        </View>
      )}
      <Text style={styles.messageTime}>
        {new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
      </Text>
    </View>
  );

  return (
    <KeyboardAvoidingView
      style={styles.container}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={90}
    >
      {/* Crop + Stage pickers */}
      <View style={styles.pickerRow}>
        {CROPS.map(c => (
          <TouchableOpacity
            key={c.value}
            style={[styles.pickerChip, selectedCrop === c.value && styles.pickerChipActive]}
            onPress={() => setSelectedCrop(c.value)}
          >
            <Text style={[styles.pickerChipText, selectedCrop === c.value && styles.pickerChipTextActive]}>
              {c.label.split(' ')[1]}
            </Text>
          </TouchableOpacity>
        ))}
      </View>
      <View style={styles.pickerRow}>
        {STAGES.map(s => (
          <TouchableOpacity
            key={s.value}
            style={[styles.pickerChip, selectedStage === s.value && styles.pickerChipActive]}
            onPress={() => setSelectedStage(s.value)}
          >
            <Text style={[styles.pickerChipText, selectedStage === s.value && styles.pickerChipTextActive]}>
              {s.label.split(' ')[1]}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Chat messages */}
      <FlatList
        ref={flatListRef}
        data={messages}
        renderItem={renderMessage}
        keyExtractor={item => item.id}
        style={styles.chatList}
        contentContainerStyle={styles.chatContent}
        onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
      />

      {/* Input area */}
      <View style={styles.inputArea}>
        {loading && <ActivityIndicator size="small" color="#2E7D32" style={styles.typingIndicator} />}
        <View style={styles.inputRow}>
          <TextInput
            style={styles.input}
            placeholder={farmer.language === 'hindi' ? 'अपना प्रश्न लिखें...' : 'तुमचा प्रश्न लिहा...'}
            value={inputText}
            onChangeText={setInputText}
            multiline
            maxLength={500}
            editable={!loading}
          />
          <TouchableOpacity
            style={[styles.sendButton, (!inputText.trim() || loading) && styles.sendButtonDisabled]}
            onPress={sendMessage}
            disabled={!inputText.trim() || loading}
          >
            <Text style={styles.sendButtonText}>▶</Text>
          </TouchableOpacity>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#f5f5f5' },
  pickerRow: { flexDirection: 'row', paddingHorizontal: 12, paddingTop: 8, gap: 8 },
  pickerChip: {
    flex: 1,
    paddingVertical: 8,
    backgroundColor: '#fff',
    borderRadius: 20,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ddd',
  },
  pickerChipActive: { backgroundColor: '#2E7D32', borderColor: '#2E7D32' },
  pickerChipText: { fontSize: 13, color: '#666', fontWeight: '500' },
  pickerChipTextActive: { color: '#fff' },
  chatList: { flex: 1 },
  chatContent: { padding: 16, paddingBottom: 8 },
  messageBubble: { maxWidth: '82%', borderRadius: 16, padding: 14, marginBottom: 12 },
  userBubble: { alignSelf: 'flex-end', backgroundColor: '#2E7D32' },
  assistantBubble: { alignSelf: 'flex-start', backgroundColor: '#fff', borderWidth: 1, borderColor: '#E0E0E0' },
  messageText: { fontSize: 15, color: '#333', lineHeight: 22 },
  userMessageText: { color: '#fff' },
  messageTextEn: { fontSize: 13, color: '#888', marginTop: 6, lineHeight: 18 },
  priorityBadge: {
    alignSelf: 'flex-start',
    backgroundColor: '#FFF3E0',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 4,
    marginTop: 8,
  },
  priorityText: { fontSize: 12, color: '#E65100', fontWeight: 'bold' },
  messageTime: { fontSize: 11, color: '#aaa', marginTop: 6, alignSelf: 'flex-end' },
  inputArea: { backgroundColor: '#fff', borderTopWidth: 1, borderTopColor: '#E0E0E0', padding: 12 },
  typingIndicator: { marginBottom: 8 },
  inputRow: { flexDirection: 'row', alignItems: 'flex-end', gap: 8 },
  input: {
    flex: 1,
    backgroundColor: '#f5f5f5',
    borderRadius: 20,
    paddingHorizontal: 16,
    paddingVertical: 12,
    fontSize: 15,
    maxHeight: 100,
  },
  sendButton: {
    backgroundColor: '#2E7D32',
    width: 44,
    height: 44,
    borderRadius: 22,
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendButtonDisabled: { opacity: 0.4 },
  sendButtonText: { color: '#fff', fontSize: 18 },
});