package com.project.InsightPrep.domain.post.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentRequest.UpdateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRow;
import com.project.InsightPrep.domain.post.entity.Comment;
import com.project.InsightPrep.domain.post.entity.SharedPost;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.mapper.CommentMapper;
import com.project.InsightPrep.domain.post.mapper.SharedPostMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    SecurityUtil securityUtil;

    @Mock
    SharedPostMapper sharedPostMapper;

    @Mock
    CommentMapper commentMapper;

    @InjectMocks
    CommentServiceImpl commentService;

    // ===== createComment =====
    @Nested
    @DisplayName("createComment")
    class CreateComment {

        @Test
        @DisplayName("성공 - 게시글 존재 + 본인 인증 OK")
        void create_success() {
            long postId = 10L;
            Member me = Member.builder().id(1L).nickname("tester").build();
            SharedPost post = SharedPost.builder().id(postId).build();

            when(securityUtil.getAuthenticatedMember()).thenReturn(me);
            when(sharedPostMapper.findById(postId)).thenReturn(post);
            // insert 시 selectKey로 id가 세팅되는 것을 흉내
            doAnswer(inv -> {
                Comment c = inv.getArgument(0);
                try {
                    var idField = Comment.class.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(c, 777L);
                } catch (Exception ignored) {}
                return null;
            }).when(commentMapper).insertComment(any(Comment.class));

            CreateDto req = new CreateDto("첫 댓글");

            CommentRes res = commentService.createComment(postId, req);

            assertThat(res.getCommentId()).isEqualTo(777L);
            assertThat(res.getContent()).isEqualTo("첫 댓글");
            assertThat(res.getAuthorId()).isEqualTo(1L);
            assertThat(res.getAuthorNickname()).isEqualTo("tester");
            assertThat(res.getPostId()).isEqualTo(postId);

            verify(securityUtil).getAuthenticatedMember();
            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).insertComment(any(Comment.class));
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void create_post_not_found() {
            long postId = 99L;
            when(sharedPostMapper.findById(postId)).thenReturn(null);

            assertThatThrownBy(() ->
                    commentService.createComment(postId, new CreateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verifyNoInteractions(commentMapper);
        }
    }

    // ===== updateComment =====
    @Nested
    @DisplayName("updateComment")
    class UpdateComment {

        @Test
        @DisplayName("성공 - 본인 댓글 & 같은 postId")
        void update_success() {
            long postId = 10L;
            long commentId = 200L;
            long me = 1L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, postId, me, "old"));
            when(securityUtil.getLoginMemberId()).thenReturn(me);
            when(commentMapper.updateContent(commentId, me, "new content")).thenReturn(1);

            commentService.updateComment(postId, commentId, new UpdateDto("new content"));

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verify(securityUtil).getLoginMemberId();
            verify(commentMapper).updateContent(commentId, me, "new content");
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void update_post_not_found() {
            long postId = 10L;
            long commentId = 200L;
            when(sharedPostMapper.findById(postId)).thenReturn(null);

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verifyNoMoreInteractions(sharedPostMapper);
            verifyNoInteractions(commentMapper, securityUtil);
        }

        @Test
        @DisplayName("실패 - 댓글 없음 → COMMENT_NOT_FOUND")
        void update_comment_not_found() {
            long postId = 10L;
            long commentId = 200L;
            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId)).thenReturn(null);

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verifyNoMoreInteractions(commentMapper);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 다른 게시글의 댓글 → COMMENT_NOT_FOUND")
        void update_wrong_postId() {
            long postId = 10L;
            long commentId = 200L;
            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            // 댓글이 다른 postId에 속함
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, 999L, 1L, "x"));

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("x"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verifyNoMoreInteractions(commentMapper);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 본인 아님 → COMMENT_FORBIDDEN")
        void update_forbidden() {
            long postId = 10L;
            long commentId = 200L;
            long owner = 1L;
            long me = 2L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, postId, owner, "x"));
            when(securityUtil.getLoginMemberId()).thenReturn(me);

            assertThatThrownBy(() ->
                    commentService.updateComment(postId, commentId, new UpdateDto("new"))
            ).isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_FORBIDDEN.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verify(securityUtil).getLoginMemberId();
            verifyNoMoreInteractions(commentMapper);
        }
    }

    // ===== deleteComment =====
    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("성공 - 본인 댓글 삭제")
        void delete_success() {
            long postId = 10L;
            long commentId = 200L;
            long me = 1L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, postId, me, "x"));
            when(securityUtil.getLoginMemberId()).thenReturn(me);
            when(commentMapper.deleteByIdAndMember(commentId, me)).thenReturn(1);

            commentService.deleteComment(postId, commentId);

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verify(securityUtil).getLoginMemberId();
            verify(commentMapper).deleteByIdAndMember(commentId, me);
        }

        @Test
        @DisplayName("실패 - 게시글 없음 → POST_NOT_FOUND")
        void delete_post_not_found() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostMapper.findById(postId)).thenReturn(null);

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.POST_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verifyNoInteractions(commentMapper, securityUtil);
        }

        @Test
        @DisplayName("실패 - 댓글 없음 → COMMENT_NOT_FOUND")
        void delete_comment_not_found() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId)).thenReturn(null);

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verifyNoMoreInteractions(commentMapper);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 다른 게시글의 댓글 → COMMENT_NOT_FOUND")
        void delete_wrong_postId() {
            long postId = 10L;
            long commentId = 200L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, 999L, 1L, "x"));

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_NOT_FOUND.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verifyNoMoreInteractions(commentMapper);
            verifyNoInteractions(securityUtil);
        }

        @Test
        @DisplayName("실패 - 본인 아님 → COMMENT_FORBIDDEN (현재 구현상 delete 쿼리는 수행됨)")
        void delete_forbidden() {
            long postId = 10L;
            long commentId = 200L;
            long owner = 1L;
            long me = 2L;

            when(sharedPostMapper.findById(postId)).thenReturn(SharedPost.builder().id(postId).build());
            when(commentMapper.findRowById(commentId))
                    .thenReturn(new CommentRow(commentId, postId, owner, "x"));
            when(securityUtil.getLoginMemberId()).thenReturn(me);
            // 구현상 먼저 deleteByIdAndMember를 호출한 뒤 소유자 검사 → 0 반환될 수 있음
            when(commentMapper.deleteByIdAndMember(commentId, me)).thenReturn(0);

            assertThatThrownBy(() -> commentService.deleteComment(postId, commentId))
                    .isInstanceOf(PostException.class)
                    .hasMessageContaining(PostErrorCode.COMMENT_FORBIDDEN.getMessage());

            verify(sharedPostMapper).findById(postId);
            verify(commentMapper).findRowById(commentId);
            verify(securityUtil).getLoginMemberId();
            verify(commentMapper).deleteByIdAndMember(commentId, me);
        }
    }
}